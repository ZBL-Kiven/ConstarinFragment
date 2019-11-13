<p align="center" >
   <img src = "https://github.com/ZBL-Kiven/ConstarinFragment/raw/master/demo/title.png"/>
   <br>
   <br>
   <a href = "http://cityfruit.io/">
   <img src = "https://img.shields.io/static/v1?label=By&message=CityFruit.io&color=2af"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven/album">
      <img src = "https://img.shields.io/static/v1?label=platform&message=Android&color=6bf"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=author&message=ZJJ&color=9cf"/>
  </a>
  <a href = "https://developer.android.google.cn/jetpack/androidx">
      <img src = "https://img.shields.io/static/v1?label=supported&message=AndroidX&color=8ce"/>
  </a>
  <a href = "https://www.android-doc.com/guide/components/android7.0.html">
      <img src = "https://img.shields.io/static/v1?label=minVersion&message=Nougat&color=cce"/>
  </a>
</p>
 
## Introduction：

###### ConstrainFragment 是为 Android 应用设计的 Fragment 多层级管理器集群框架 ，使用 [java]() 和 [Kotlin]() 语言混合开发，它完全基于原生 API 实现，兼容 ‘内嵌’ 、‘压栈’、‘模式定义’、‘生命周期管理’ 等完备功能。底层基于 hash 链表实现，性能卓越，稳定灵活。不依赖任何第三方库，是目前 Fragment 批处理唯一一个落地框架。

> ConstrainFragment 已为 AndroidX 提供，请升级至 V1.0.1 版本。

## Features：

* 支持：Fragments 自动 压/出 栈。
* 支持：由原生 FragmentActivity 或 Fragment 直接启动 ConstrainFragment 活动。
* 支持：简易注解声明 Fragment 应用场景适配、启动模式等。
* 支持：固定栈底或浮动栈底。
* 支持：单层、多层、混合压栈, 支持内联栈。
* 支持：Bundle 序列化传参。
* 支持：onCreated() - onStarted() / onRestart() - onResumed() - onPaused() - onStop() -onDestroyed() 等 Activity 生命周期。
* 支持：自定义栈、插入栈、删除栈、指定栈、回退栈。
* 支持：栈底获取。
* 支持：数据保留或清除。

> 单元测试

- 即将覆盖

## demo：

使用 Android 设备下载 [APK](https://github.com/ZBL-Kiven/ConstarinFragment/raw/master/demo/demo.apk) 安装包安装 demo 即可把玩。

## Installation :


ConstrainFragment 已发布至私有仓库，你可以使用如下方式安装它：

> by dependencies:

```grovy
repo{
     maven (url = "https://nexus.i-mocca.com/repository/cf_core")
}

implementation 'com.cf.core:constrainFragmnet:+'
```

> by [aar](https://nexus.i-mocca.com/repository/cf_core/com/cf/core/constrainFragment/1.0.0/constrainFragment-1.0.0.aar) import:

```
copy the aar file in your app libs to use
```

> by [module](https://github.com/ZBL-Kiven/ConstarinFragment/archive/master.zip) copy:
 
```
copy the module 'zj_cf' into your app

implementation project(":zj_cf")

```

## Usage:

> 使用 LinkageFragment ：

```
1、在任意地方调用即可启动显示：
BaseFragmentManager(ctx, container:ViewGroup, curItem:Int, indicators:ViewGrop, FragmentA(), FragmentB(), FragmentN()...){
        //when empty stack
}

*注：需要支持切换显示的 Fragment 继承自 BaseLinkageFragment
FragmentA extend BaseLinkageFragment...
``` 

BaseFragmentManager 允许通过 TAB 控制显示多个 Fragment，不同于普通开源库的是，它具有完善的生命周期，且支持直接在任意 Item 开启 Fragment 栈，同时它可以更方便的使用和具备更强的扩展。

> 使用 ConstrainFragment：

```
//必须注解
@Constrain(id:String , mod:BackMode)

//可选注解
@LaunchMode(mod:LaunchMode) 

//activity 或 fragment
startFragment(cls:Class , b:Bundle)


* 注：适用于链式压栈的 Fragment 需继承自 ConstrainFragment
yourFragment : ConstrainFragment()
```
* 注：

* id 为标识这个 Fragment 在栈内的唯一 ID ，该 ID 亦可用于从栈内获取或查询对应的 Fragment 。
 
* BackMod 为 定义好的 Int 值，使用者仅能传人 lasting（1） only-once（0） 两个值，其中，
  
  1、对于 only-once 的解释： @property ONLY_ONCE the fragment has created only by used , and it'll destroy when close
 
  2、对于 lasting 的解释： @property LASTING if the manager is running or activity is living , the fragment will exists in long at stack
 
* LaunchMode 为定义该 Fragment 启动场景约束，由使用者根据业务场景使用。包含如下规则：
 
 1、@property STACK ：this fragment will back by an ordered stack,for added, it'll remove all task stack on the top of self
 
 2 @property FOLLOW ： liked STACK , but it won't remove any task stack , although the same one in the bottom of self
 
 3 @property CLEAR_BACK_STACK ： never created backed stack , only back to home when it closed .@see ConstrainHome

## Func

> 结束：
 
```
  finish()
```
在任意地点调用 Finish 均可结束出栈并关闭该活动，同时唤起上一个 Fragment 或 关闭栈（栈为空时）。
  
> 获取栈顶：

```
getTop()
```
在任何时候使用此方法获取当前 Fragment 栈顶（如存在）。

> 设置回退栈

```
setPrevious(cls:Class, b:Bundle)
```
在任何时候使用此方法设置当前 Fragment 回退栈。

> 清空栈

```
clearStack()
```
在任何时候调用方法清空当前 Fragment 栈。

## SimpleDemo

> 项目中的任意 Activity

```java 
class MainActivity:AppCompatActivity(){

   fun a(){
     val b = Bundle()
     b.putStringExtra("hello"," i'm new constrain fragment stack")
     startFragment(FragmentA::class.java, b)
   }    
   
   fun b(){
     BaseFragmentManager(this,frgContainer,2,llIndicators,BottomA(),BottomB(),BottomC(),BottomD(),BottomE())
   }
}

```

> 定义的其中一个 ConstrainFragment

```kotlin
@Constrain(id = "FragmentA", backMode = BackMode.LASTING)
@LaunchMode(LaunchMode.Stack)
class FragmentA : ConstrainFragment() {

   override fun onPostValue(b:Bundle){
      print(b.getStringExtra("hello").toString)
   }
   
   fun a(){
      setPrevious(FragmentB::class.java,null)
   }
   
   fun b(){
      finish()
   }
   
   fun c(){
     startFragment(FragmentC::class.java,null)//in same tasks
   }
   
   fun d(){
     startFragmentByNewTask(FragmentC::class.java,null)//on new task
   }
   
   fun e(){
      getStackTop()?.finish()//获取栈顶 and doSomething
   }
}
```

### Contributing

Contributions are very welcome 🎉

### Licence :  

Copyright (c) 2019 CityFruit zjj0888@gmail.com<br>
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.<br>
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
