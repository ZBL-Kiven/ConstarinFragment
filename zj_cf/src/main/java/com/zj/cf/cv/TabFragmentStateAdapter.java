package com.zj.cf.cv;

import static androidx.lifecycle.Lifecycle.State.RESUMED;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.collection.LongSparseArray;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.StatefulAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.zj.cf.fragments.BaseTabFragment;

import java.util.Set;

@SuppressWarnings("unused")
public abstract class TabFragmentStateAdapter<F extends BaseTabFragment> extends RecyclerView.Adapter<TabFragmentViewHolder> implements StatefulAdapter {
    // State saving config
    private static final String KEY_PREFIX_FRAGMENT = "f#";
    private static final String KEY_PREFIX_STATE = "s#";

    // BaseTabFragment GC config
    private static final long GRACE_WINDOW_TIME_MS = 10_000; // 10 seconds

    final Lifecycle mLifecycle;
    final FragmentManager mFragmentManager;

    // BaseTabFragment bookkeeping
    final LongSparseArray<F> mFragments = new LongSparseArray<>();
    private final LongSparseArray<F.SavedState> mSavedStates = new LongSparseArray<>();
    private final LongSparseArray<Integer> mItemIdToViewHolder = new LongSparseArray<>();

    private FragmentMaxLifecycleEnforcer mFragmentMaxLifecycleEnforcer;

    boolean mIsInGracePeriod = false;
    private boolean mHasStaleFragments = false;

    /**
     * @param fragmentActivity if the {@link ViewPager2} lives directly in a
     *                         {@link FragmentActivity} subclass.
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(BaseTabFragment)
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(FragmentManager, Lifecycle)
     */
    public TabFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        this(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle());
    }

    /**
     * @param fragment if the {@link ViewPager2} lives directly in a {@link BaseTabFragment} subclass.
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(FragmentActivity)
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(FragmentManager, Lifecycle)
     */
    public TabFragmentStateAdapter(@NonNull F fragment) {
        this(fragment.getChildFragmentManager(), fragment.getLifecycle());
    }

    /**
     * @param fragmentManager of {@link ViewPager2}'s host
     * @param lifecycle       of {@link ViewPager2}'s host
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(FragmentActivity)
     * @see TabFragmentStateAdapter#TabFragmentStateAdapter(BaseTabFragment)
     */
    public TabFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        mFragmentManager = fragmentManager;
        mLifecycle = lifecycle;
        super.setHasStableIds(true);
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (mFragmentMaxLifecycleEnforcer != null) return;
        mFragmentMaxLifecycleEnforcer = new FragmentMaxLifecycleEnforcer();
        mFragmentMaxLifecycleEnforcer.register(recyclerView);
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mFragmentMaxLifecycleEnforcer.unregister(recyclerView);
        mFragmentMaxLifecycleEnforcer = null;
    }

    /**
     * Provide a new BaseTabFragment associated with the specified position.
     * <p>
     * The adapter will be responsible for the BaseTabFragment lifecycle:
     * <ul>
     *     <li>The BaseTabFragment will be used to display an item.</li>
     *     <li>The BaseTabFragment will be destroyed when it gets too far from the viewport, and its state
     *     will be saved. When the item is close to the viewport again, a new BaseTabFragment will be
     *     requested, and a previously saved state will be used to initialize it.
     * </ul>
     *
     * @see ViewPager2#setOffscreenPageLimit
     */
    public abstract @NonNull
    F createFragment(int position);

    @NonNull
    @Override
    public final TabFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TabFragmentViewHolder.create(parent);
    }

    @Override
    public final void onBindViewHolder(final @NonNull TabFragmentViewHolder holder, int position) {
        final long itemId = holder.getItemId();
        final int viewHolderId = holder.itemView.getId();
        final Long boundItemId = itemForViewHolder(viewHolderId); // item currently bound to the VH
        if (boundItemId != null && boundItemId != itemId) {
            removeFragment(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }

        mItemIdToViewHolder.put(itemId, viewHolderId); // this might overwrite an existing entry
        ensureFragment(position);
        final FrameLayout container = (FrameLayout) holder.itemView;
        if (ViewCompat.isAttachedToWindow(container)) {
            if (container.getParent() != null) {
                throw new IllegalStateException("Design assumption violated.");
            }
            container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (container.getParent() != null) {
                        container.removeOnLayoutChangeListener(this);
                        placeFragmentInViewHolder(holder);
                    }
                }
            });
        }

        gcFragments();
    }

    // to avoid creation of a synthetic accessor
    void gcFragments() {
        if (!mHasStaleFragments || shouldDelayFragmentTransactions()) {
            return;
        }

        // Remove Fragments for items that are no longer part of the data-set
        Set<Long> toRemove = new ArraySet<>();
        for (int ix = 0; ix < mFragments.size(); ix++) {
            long itemId = mFragments.keyAt(ix);
            if (!containsItem(itemId)) {
                toRemove.add(itemId);
                mItemIdToViewHolder.remove(itemId); // in case they're still bound
            }
        }

        // Remove Fragments that are not bound anywhere -- pending a grace period
        if (!mIsInGracePeriod) {
            mHasStaleFragments = false; // we've executed all GC checks

            for (int ix = 0; ix < mFragments.size(); ix++) {
                long itemId = mFragments.keyAt(ix);
                if (!isFragmentViewBound(itemId)) {
                    toRemove.add(itemId);
                }
            }
        }

        for (Long itemId : toRemove) {
            removeFragment(itemId);
        }
    }

    private boolean isFragmentViewBound(long itemId) {
        if (mItemIdToViewHolder.containsKey(itemId)) {
            return true;
        }

        F fragment = mFragments.get(itemId);
        if (fragment == null) {
            return false;
        }

        View view = fragment.getView();
        if (view == null) {
            return false;
        }

        return view.getParent() != null;
    }

    private Long itemForViewHolder(int viewHolderId) {
        Long boundItemId = null;
        for (int ix = 0; ix < mItemIdToViewHolder.size(); ix++) {
            if (mItemIdToViewHolder.valueAt(ix) == viewHolderId) {
                if (boundItemId != null) {
                    throw new IllegalStateException("Design assumption violated: " + "a ViewHolder can only be bound to one item at a time.");
                }
                boundItemId = mItemIdToViewHolder.keyAt(ix);
            }
        }
        return boundItemId;
    }

    private void ensureFragment(int position) {
        long itemId = getItemId(position);
        if (!mFragments.containsKey(itemId)) {
            F newFragment = createFragment(position);
            newFragment.setInitialSavedState(mSavedStates.get(itemId));
            mFragments.put(itemId, newFragment);
        }
    }

    @Override
    public final void onViewAttachedToWindow(@NonNull final TabFragmentViewHolder holder) {
        placeFragmentInViewHolder(holder);
        gcFragments();
    }

    void placeFragmentInViewHolder(@NonNull final TabFragmentViewHolder holder) {
        F fragment = mFragments.get(holder.getItemId());
        if (fragment == null) {
            throw new IllegalStateException("Design assumption violated.");
        }
        FrameLayout container = holder.getContainer();
        View view = fragment.getView();

        /*
        possible states:
        - fragment: { added, notAdded }
        - view: { created, notCreated }
        - view: { attached, notAttached }

        combinations:
        - { f:added, v:created, v:attached } -> check if attached to the right container
        - { f:added, v:created, v:notAttached} -> attach view to container
        - { f:added, v:notCreated, v:attached } -> impossible
        - { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        - { f:notAdded, v:created, v:attached } -> illegal state
        - { f:notAdded, v:created, v:notAttached } -> illegal state
        - { f:notAdded, v:notCreated, v:attached } -> impossible
        - { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
         */

        // { f:notAdded, v:created, v:attached } -> illegal state
        // { f:notAdded, v:created, v:notAttached } -> illegal state
        if (!fragment.isAdded() && view != null) {
            throw new IllegalStateException("Design assumption violated.");
        }

        // { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        if (fragment.isAdded() && view == null) {
            scheduleViewAttach(fragment, container);
            return;
        }

        // { f:added, v:created, v:attached } -> check if attached to the right container
        if (fragment.isAdded() && view.getParent() != null) {
            if (view.getParent() != container) {
                addViewToContainer(fragment, view, container);
            }
            return;
        }

        // { f:added, v:created, v:notAttached} -> attach view to container
        if (fragment.isAdded()) {
            addViewToContainer(fragment, view, container);
            return;
        }

        // { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
        if (!shouldDelayFragmentTransactions()) {
            scheduleViewAttach(fragment, container);
            mFragmentManager.beginTransaction().add(fragment, "f" + holder.getItemId()).setMaxLifecycle(fragment, STARTED).commitNow();
            mFragmentMaxLifecycleEnforcer.updateFragmentMaxLifecycle(false);
        } else {
            if (mFragmentManager.isDestroyed()) {
                return; // nothing we can do
            }
            mLifecycle.addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (shouldDelayFragmentTransactions()) {
                        return;
                    }
                    source.getLifecycle().removeObserver(this);
                    if (ViewCompat.isAttachedToWindow(holder.getContainer())) {
                        placeFragmentInViewHolder(holder);
                    }
                }
            });
        }
    }

    private void scheduleViewAttach(final F fragment, @NonNull final FrameLayout container) {
        // After a config change, Fragments that were in FragmentManager will be recreated. Since
        // ViewHolder container ids are dynamically generated, we opted to manually handle
        // attaching BaseTabFragment views to containers. For consistency, we use the same mechanism for
        // all BaseTabFragment views.
        mFragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {

            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                if (f == fragment) {
                    fm.unregisterFragmentLifecycleCallbacks(this);
                    addViewToContainer(cast(f), v, container);
                }
            }
        }, false);
    }

    // to avoid creation of a synthetic accessor
    public void addViewToContainer(@NonNull F f, @NonNull View v, @NonNull FrameLayout container) {
        if (container.getChildCount() > 1) {
            throw new IllegalStateException("Design assumption violated.");
        }

        if (v.getParent() == container) {
            return;
        }

        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        if (v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
        }

        container.addView(v);
    }

    @Override
    public final void onViewRecycled(@NonNull TabFragmentViewHolder holder) {
        final int viewHolderId = holder.getContainer().getId();
        final Long boundItemId = itemForViewHolder(viewHolderId); // item currently bound to the VH
        if (boundItemId != null) {
            removeFragment(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }
    }

    @Override
    public final boolean onFailedToRecycleView(@NonNull TabFragmentViewHolder holder) {
        return true;
    }

    private void removeFragment(long itemId) {
        F fragment = mFragments.get(itemId);

        if (fragment == null) {
            return;
        }

        if (fragment.getView() != null) {
            ViewParent viewParent = fragment.getView().getParent();
            if (viewParent != null) {
                ((FrameLayout) viewParent).removeAllViews();
            }
        }

        if (!containsItem(itemId)) {
            mSavedStates.remove(itemId);
        }

        if (!fragment.isAdded()) {
            mFragments.remove(itemId);
            return;
        }

        if (shouldDelayFragmentTransactions()) {
            mHasStaleFragments = true;
            return;
        }

        if (fragment.isAdded() && containsItem(itemId)) {
            mSavedStates.put(itemId, mFragmentManager.saveFragmentInstanceState(fragment));
        }
        mFragmentManager.beginTransaction().remove(fragment).commitNow();
        mFragments.remove(itemId);
    }

    // to avoid creation of a synthetic accessor
    boolean shouldDelayFragmentTransactions() {
        return mFragmentManager.isStateSaved();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < getItemCount();
    }

    @Override
    public final void setHasStableIds(boolean hasStableIds) {
        throw new UnsupportedOperationException("Stable Ids are required for the adapter to function properly, and the adapter " + "takes care of setting the flag.");
    }

    @Override
    public final @NonNull
    Parcelable saveState() {
        Bundle savedState = new Bundle(mFragments.size() + mSavedStates.size());

        for (int ix = 0; ix < mFragments.size(); ix++) {
            long itemId = mFragments.keyAt(ix);
            F fragment = mFragments.get(itemId);
            if (fragment != null && fragment.isAdded()) {
                String key = createKey(KEY_PREFIX_FRAGMENT, itemId);
                mFragmentManager.putFragment(savedState, key, fragment);
            }
        }

        for (int ix = 0; ix < mSavedStates.size(); ix++) {
            long itemId = mSavedStates.keyAt(ix);
            if (containsItem(itemId)) {
                String key = createKey(KEY_PREFIX_STATE, itemId);
                savedState.putParcelable(key, mSavedStates.get(itemId));
            }
        }

        return savedState;
    }

    @Override
    public final void restoreState(@NonNull Parcelable savedState) {
        if (!mSavedStates.isEmpty() || !mFragments.isEmpty()) {
            throw new IllegalStateException("Expected the adapter to be 'fresh' while restoring state.");
        }

        Bundle bundle = (Bundle) savedState;
        if (bundle.getClassLoader() == null) {
            bundle.setClassLoader(getClass().getClassLoader());
        }

        for (String key : bundle.keySet()) {
            if (isValidKey(key, KEY_PREFIX_FRAGMENT)) {
                long itemId = parseIdFromKey(key, KEY_PREFIX_FRAGMENT);
                F fragment = cast(mFragmentManager.getFragment(bundle, key));
                mFragments.put(itemId, fragment);
                continue;
            }

            if (isValidKey(key, KEY_PREFIX_STATE)) {
                long itemId = parseIdFromKey(key, KEY_PREFIX_STATE);
                F.SavedState state = bundle.getParcelable(key);
                if (containsItem(itemId)) {
                    mSavedStates.put(itemId, state);
                }
                continue;
            }

            throw new IllegalArgumentException("Unexpected key in savedState: " + key);
        }

        if (!mFragments.isEmpty()) {
            mHasStaleFragments = true;
            mIsInGracePeriod = true;
            gcFragments();
            scheduleGracePeriodEnd();
        }
    }

    private void scheduleGracePeriodEnd() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = () -> {
            mIsInGracePeriod = false;
            gcFragments(); // good opportunity to GC
        };

        mLifecycle.addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    handler.removeCallbacks(runnable);
                    source.getLifecycle().removeObserver(this);
                }
            }
        });

        handler.postDelayed(runnable, GRACE_WINDOW_TIME_MS);
    }

    // Helper function for dealing with save / restore state
    private static @NonNull
    String createKey(@NonNull String prefix, long id) {
        return prefix + id;
    }

    // Helper function for dealing with save / restore state
    private static boolean isValidKey(@NonNull String key, @NonNull String prefix) {
        return key.startsWith(prefix) && key.length() > prefix.length();
    }

    // Helper function for dealing with save / restore state
    private static long parseIdFromKey(@NonNull String key, @NonNull String prefix) {
        return Long.parseLong(key.substring(prefix.length()));
    }

    /**
     * Pauses (STARTED) all Fragments that are attached and not a primary item.
     * Keeps primary item F RESUMED.
     */
    class FragmentMaxLifecycleEnforcer {
        private ViewPager2.OnPageChangeCallback mPageChangeCallback;
        private RecyclerView.AdapterDataObserver mDataObserver;
        private LifecycleEventObserver mLifecycleObserver;
        private ViewPager2 mViewPager;

        private long mPrimaryItemId = NO_ID;

        void register(@NonNull RecyclerView recyclerView) {
            mViewPager = inferViewPager(recyclerView);

            // signal 1 of 3: current item has changed
            mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    updateFragmentMaxLifecycle(false);
                }

                @Override
                public void onPageSelected(int position) {
                    updateFragmentMaxLifecycle(false);
                }
            };
            mViewPager.registerOnPageChangeCallback(mPageChangeCallback);

            // signal 2 of 3: underlying data-set has been updated
            mDataObserver = new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    updateFragmentMaxLifecycle(true);
                }
            };
            registerAdapterDataObserver(mDataObserver);

            // signal 3 of 3: we may have to catch-up after being in a lifecycle state that
            // prevented us to perform transactions
            mLifecycleObserver = (source, event) -> updateFragmentMaxLifecycle(false);
            mLifecycle.addObserver(mLifecycleObserver);
        }

        void unregister(@NonNull RecyclerView recyclerView) {
            ViewPager2 viewPager = inferViewPager(recyclerView);
            viewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
            unregisterAdapterDataObserver(mDataObserver);
            mLifecycle.removeObserver(mLifecycleObserver);
            mViewPager = null;
        }

        void updateFragmentMaxLifecycle(boolean dataSetChanged) {
            if (shouldDelayFragmentTransactions()) {
                return;
            }

            if (mViewPager.getScrollState() != ViewPager2.SCROLL_STATE_IDLE) {
                return; // do not update while not idle to avoid jitter
            }

            if (mFragments.isEmpty() || getItemCount() == 0) {
                return; // nothing to do
            }

            final int currentItem = mViewPager.getCurrentItem();
            if (currentItem >= getItemCount()) {
                return;
            }

            long currentItemId = getItemId(currentItem);
            if (currentItemId == mPrimaryItemId && !dataSetChanged) {
                return; // nothing to do
            }

            F currentItemFragment = mFragments.get(currentItemId);
            if (currentItemFragment == null || !currentItemFragment.isAdded()) {
                return;
            }

            mPrimaryItemId = currentItemId;
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            F toResume = null;
            for (int ix = 0; ix < mFragments.size(); ix++) {
                long itemId = mFragments.keyAt(ix);
                F fragment = mFragments.valueAt(ix);

                if (!fragment.isAdded()) {
                    continue;
                }

                if (itemId != mPrimaryItemId) {
                    transaction.setMaxLifecycle(fragment, STARTED);
                } else {
                    toResume = fragment; // itemId map key, so only one can match the predicate
                }

                fragment.setMenuVisibility(itemId == mPrimaryItemId);
            }
            if (toResume != null) { // in case the F wasn't added yet
                transaction.setMaxLifecycle(toResume, RESUMED);
            }

            if (!transaction.isEmpty()) {
                transaction.commitNow();
            }
        }

        @NonNull
        private ViewPager2 inferViewPager(@NonNull RecyclerView recyclerView) {
            ViewParent parent = recyclerView.getParent();
            if (parent instanceof ViewPager2) {
                return (ViewPager2) parent;
            }
            throw new IllegalStateException("Expected ViewPager2 instance. Got: " + parent);
        }
    }

    /**
     * Simplified {@link RecyclerView.AdapterDataObserver} for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract static class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            onChanged();
        }

        @Override
        public final void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> F cast(T t) {
        return (F) t;
    }
}
