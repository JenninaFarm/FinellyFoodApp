# FinellyFoodApp

This project is for native mobile development and it is based on the wishes of my friend's company.

## Name

Jennina Färm

## Topic

### more defined idea for this course

Mobile application for android where you can read bar codes from groceries. Bar code reader is using CameraX API for using decive camera and ML kit API for recognizing barcodes from live feed.

From the barcode rawdata application fill fetch data for basic diet information like carps, proteion and fat. For this app is using FatSecret Platform API.

#### Links to API:s

- https://platform.fatsecret.com/api/
- https://developer.android.com/training/camerax
- https://developers.google.com/ml-kit

### Ideas to continue

Mobile application can be used as a food diery. From fetched diet info user can add servings to his/her food diery.

Since FatSecret don't have most of the bar codes for products originated from Finland, there sould be a way to find ingredients and product by name to complite user's opportunity to add those to the food diery.

## Target

Android/Kotlin

## Google Play link

http://... (will be updated when ready)

### Release 1: 2021-05-12 features

- User is able to give permission to use device camera
- User is able to read EAN-13, EAN-8 and UPC-A bar codes with the live camera feed
- User is able to see read bar code from the bottom of the screen

### Release 2: 2021-05-21 features

- User is able to see full ui with bottom menu
- User is able to navigate to barcode scanner, dashboard and home
- User is able to read EAN-13, EAN-8 and UPC-A barcodes with the live camera feed and ui navigates to the dashboard to show the results
- In dashboard user is able to see the read barcode
- In dashboard user is able to see nutrient content of French Toast per 100g once barcode is read

### Known Bugs

- App don't receive information of the barcode since the App don't have needed credentials yet
  - This is why it only shows the French Toast at the moment
- Gives error code: E/libc: Access denied finding property "persist.vendor.camera.privapp.list"
  - It doesn't seem to effect of the usage though
  - Used device: OnePlus6

#### Screencast

Here is a demonstration about UI, relevant parts of the code, known bugs and self-evaluation

- https://youtu.be/Rvt4sgFklIM
