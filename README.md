# Orchidarium App 


It is my Udacity Android Nanodegree Capstone Project.

Orchidarium App is online shop that offers the best orchids from around the world anywhere and anytime. It is for everyone who admires the unearthly beauty of exotic flowers and would like to acquire the most beautiful of them. 

It also provides a tool for authorized users who are store administrators to manage the stock available to order by customers.

There are two product flavors in this application:

    productFlavors {
        admin {
            dimension "version"
            applicationIdSuffix ".admin"
            ...
        }
        user {
            dimension "version"
            applicationIdSuffix ".user"
            ...
        }
        
 
The "admin" version has activities to add orchid’s photos, set the retail price, and then put it up for sale. Administrators need to be  authenticated by email or Google account.

The "user" version provides customers of the store with photos of the orchid, a price and brief description. Users can choose flowers they like into a shopping cart and send an order by email. For customers authorization is not required.


Orchidarium App uses Realtime Database, Cloud Storage, Authentication and Notification Firebase Services.
