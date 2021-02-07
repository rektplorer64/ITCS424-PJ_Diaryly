
<div align="center">

<img width="231" height="128" src="./repo_metadata/logo.png" alt="logo">

<span>

`COURSE  PROJECT`

</span>

# Diaryly

<p>An Android diary app that allows you to add text and images in each entry with an ability to sync with Firebase.</p>
</div>

![Project Hero Image](/repo_metadata/hero_image.png)

## 🚩 Motivations
As part of the course project of `ITCS424 - Wireless and Mobile Computing`, a group of 3 students must 
implement a functional Android application using Android framework with Kotlin. According to the project description,
the app must satisfies the following requirements:
- Support for both Thai and English localization
- Has User registration and authentication
- Has Room database or Firebase integration
- At least 3 of the following features:
    - Tabs
    - Linking to Camera
    - Linking to network
    - Animated images
    - Location services and maps
    - Preferences
    - Dialogs
    - Google Map
    - `RecyclerView` or `CardView`
    - Other advanced Android features

## ✨ What we did
We created a diary application that allows users to record their actions based on two alignments: ***Good*** or ***Bad*** actions.

## ⚠ This README is under construction
### But you can read more about my project here: [PDF Link]: https://github.com/rektplorer64/ITCS424-PJ_Diaryly/blob/master/reports/checkpoint_5_report_final.pdf

## 😀 Features
- [x] **User registration & User login**
    - Using an Email address and a password
    - Add an profile image
    - Add a unique nickname
    - Has form validation and validate-as-you-type feature
    - Backed by Firebase Authentication
- [x] **Cloud data synchronization**
    - Early versions uses Room database, but later uses Firebase real-time database.  
- [x] **Beautiful tabbed layout**
- [x] **Diary entry**
    - Tagging
    - Text editing
    - Image embedding
    - Geolocation embedding
    - Accent color selection
    - Flag the status as ***good*** or ***bad*** and accumulate points.
    
## ✨ Implementation details
### 🛠 Implementation Facts
Here is a quick overview of the code base.
1. Architecture: ***MVVM*** as recommended by Google
2. The project is completely written in **Kotlin**.
    - Extension function is used extensively for util methods.
    - Coroutine is initially used but it causes deadlock to an early version which were still using Room database.
3. Data is shared between fragments using **`ViewModels`**.
4. Custom `TabView` is implemented by combining several Android View classes such as `AppBar`, `TabLayout`.
5. `Fragments` are heavily used.
6. Android Navigation is used for planning app screen changes. No more `FragmentManager`.
7. There is a separation of concern between data layer and the UI.
    - Firebase-related operation has its own singleton.
    - The UI do not call Firebase operation directly.
8. `RxJava` and `LiveData` is heavily used together.
    - `RxJava` for the data layer.
    - `LiveData` for the UI layer.
    - A transformer between RxJava and LiveData is implemented.
9. Images were loaded by using `Glide`.
10. `WorkManager` is used to upload the image to `Firebase Storage`.
11. `Google Map` API and location service are used.

### 🧩 Architecture
![Diaryly Architecture](/repo_metadata/architecture.png)
1. ***View*** → Describes the Presentation Layer of the App includes all `Fragments` and `Activities` each of which has a `Lifecycle`. Please note that each fragment can be nested.
2. ***ViewModel*** → Describes a data holder for each view. `ViewModels` can persist data that is usually garbage collected by the garbage collector when a view changes to a certain state. For example, if there is a text field filled with texts. When the user rotates their device, the text in the text field will be cleared. Moreover, ViewModel can be used to communicate or share data between fragments; therefore, `ViewModel` is one of the main tools we used in this project. 
3. ***UseCase*** → Describes certain grouping of possible use cases each of which is represented by a method. Each method wraps a function call that enables robust error handling and concurrency powered by `RxJava` and Koltin's `coroutine`. 
4. ***Model*** → It acts as a single source of truth (data) for the entire app. Describes the layer that integrates multiple data sources such as room database, Google Firebase. This layer usually labelled as `Repo` or `Repository`. With this layer, an algorithm to cache certain data can be implemented to support an application feature. This layer can be broken down into components as follow: 
    - ***Room database*** → A app-private device-local database. We have implemented most of our room database, but later throw it away since there are complication, limitations and requirements such as the need to authenticate users or the need to synchronize the data.
    - ***Firebase*** → We have migrated the app from Local Room database into `Firebase`. `Firebase Authentication`, `Realtime Database` and `Storage` are used to enable robust authentication, database and file storage. 

## 🤞 User Interactions
### 👍 Overview
Our application can be segmented into 3 parts as follows:
1.	***Out-of-box experience (OOBE)*** part includes all sign in and registration flow of the application.
2.	***Diary Book (Main)*** part includes navigation and the process of browsing diary entries.
3.	***Diary Editor*** part includes the process of creating or editing a diary entry.

### Out-of-box experience (OOBE)
In this section, the activity and fragments that related to user login or registration will be explained in details…
![OOBE user flow](/repo_metadata/OOBE-flow.png)
#### Overview
When a user launches the app for the first time, they will be greeted by a splash screen and the main landing page as illustrated on the `LoginActivity`. 
The User can select to either login with an existing user account or register a new account. 
It can be observed that the `LoginActivity` has 4 fragments that responsible for each step of logging 
in or registration. This activity actually has only `FragmentContainerView` that can only host a 
fragment at a time. We decided to use it to create a seamless experience in login and register. 
To use the `FragmentContainerView`, we must specify a navigation XML as in the figure above.

### Diary Book
#### Overview
Diary Book is the main section of the application because it contains various elements that can be
 used to navigate to other parts of the application. The main container of this section is 
 `MainActivity` which contains various `Fragments` inside. We achieve so by using the new navigation 
 library from `AndroidX`. 
![Diary book user flow](/repo_metadata/DiaryBook-flow.png)
#### After logging in or reopen the app
The user will be directed into the `DiaryFragment` inside the `MainActivity` as illustrated in figure 15. 
In the `MainActivity`, there are 5 navigation buttons at the bottom of the screen most of which 
can be pressed to navigate. There are 5 buttons from left to right as follows: 
Diary, Dashboard, Overview, Search and Goal. **Please note that some of the features are not implemented.** 

#### `DiaryFragment`
![DiaryFragment](/repo_metadata/DiaryFragment.png)
We considered `DiaryFragment` to be the main screen because it contains the main interaction part 
of the app. As displayed in the figure above, the screen allows user to create a new diary entry by 
pressing the button on the bottom right corner. The user is also allowed to navigate to each 
date by clicking at the numbered tab on the top. Moreover, use can also scroll the tab or click at 
the overline text to select a time window to display as in the figure below (Left most).
Powered by `ViewPager2`, users can also swipe to navigate between tab as in figure below (Second left most).
In this page, we also have a state indicator for users. So far there are indicator for loading 
and empty states as illustrated below (First and second right most).

![DiaryFragment interactions](/repo_metadata/DiaryFragment-details.png)

#### `SearchFragment`

![SearchFragment](/repo_metadata/SearchFragment.png)

After the user clicked on the search icon on the bottom navigation bar, the search screen will be displayed. 
With it the user is allowed to search their diary entry by the title name. This feature is using 
basic Firebase database commands as displayed in the figure above.

```kotlin
// Snippet of Code that allows partial search for the entry title.
databaseRef
    .orderByChild("title")
    .startAt(title)
    .endAt(title + "\uf8ff")
```

#### `DiaryContentDisplayFragment`

![DiaryContentDisplayFragment](/repo_metadata/DiaryContentDisplayFragment.png)

After the user select a `RecyclerView` Entry in either `SearchFragment` or `DiaryFragment`, 
the user will be redirected to the `EntryDisplayActivity` that contains `DiaryContentDisplayFragment`.

This page allows users to view and interact with their diary entry. The user can also tap on tabs 
above to navigate between entries on the same date. Please note that the user can also scroll 
down to see all of the content.

### Diary Editor
#### Overview
Diary Editor allows users to create or edit a diary entry. The main container of this section is 
`EntryEditorActivity` which contains `EntryEditorFragment` that facilitate the logic and UI for the 
user to edit or create an entry. To enable reactive UI, similarly to other parts of this app, 
we heavily utilized `LiveData` that allows us to observe its values and reactively change UI 
upon any changes.

![EntryEditFragment](/repo_metadata/EntryEditFragment.png)

`EntryEditorActivity` is an activity that hosts a fragment named `EntryEditorFragment`. `EntryEditorFragment` 
allows users to add a new diary entry that can consist of an image, text, geolocation coordinate 
and tags. `EntryEditActivity` can be accessed from the main page by using the add floating action 
button on the bottom right corner. This screen also contains a bottom collapsible sheet 
that will expand when a user press the expand button. 