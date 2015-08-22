Asset-Pipeline CDN Gradle Plugin
================================

# Introduction

The Asset-Pipeline CDN Plugin provides a [Gradle](http://gradle.org) task to automatically upload [Asset-Pipeline](http://github.com/bertramdev/asset-pipeline-core) static assets to CDNs, primarily for [Grails](http://grails.org) apps (however not mandatory).
It can easily be integrated to a build pipeline for continuous delivery/deployment.

You should always use a CDN to host all your app static assets:

- **great for your users**: faster browser page rendering thanks to CDN,
- **great for your servers**: less static requests to handle = increased load capabilities.

Undercover, it uses [Karman](http://github.com/bertramdev/karman) libs to upload files to various Cloud Storage Services.

It adds the following [Gradle](http://gradle.org) task:

- *uploadAssets* to upload assets to a CDN directory/bucket.

Kudos to *David Estes* for [Asset-Pipeline](http://github.com/bertramdev/asset-pipeline-core) and [Karman](http://grails.org/plugin/karman) libs as well as his feedback on this one!

Note: for this initial release, only *S3* provider is supported.

# Installation

Declare the plugin dependency in the `build.gradle`, as shown here:

```groovy
plugins {
   id "com.bertramlabs.plugins.asset-pipeline" version "2.4.2"
   id "agorapulse.plugins.asset-pipeline-cdn" version "0.1.1"
}
```


# Config

Add your CDN providers config in **build.gradle**.

```groovy
// Single provider
assetsCdn {
    provider = 's3' // Karman provider
    accessKey = '{MY_S3_ACCESS_KEY}'
    secretKey = '{MY_S3_SECRET_KEY}'
    region: 'us-east-1',
    directory = 'my-bucket'
    storagePath = "assets/${project.name}-${project.version}/" // This is just a prefix example
    expires = 365 // Expires in 1 year (value in days)
    gzip = true
}

// Or multiple providers
assetsCdn {
    expires = 365 // Expires in 1 year (value in days)
    gzip = true
    providers = [
        [
            provider: 's3',
            accessKey: '{MY_S3_ACCESS_KEY}',
            secretKey: '{MY_S3_SECRET_KEY}',
            region: 'us-east-1',
            directory: 'my-s3-bucket',
            storagePath: "assets/${project.name}-${project.version}/", // This is just a prefix example
        ],
        [
            provider: 'gae', // Fictive provider
            accessKey: '{MY_GAE_ACCESS_KEY}',
            secretKey: '{MY_GAE_SECRET_KEY}',
            region: 'us-east-1',
            directory: 'my-gae-bucket',
            storagePath: "assets/${project.name}-${project.version}/", // This is just a prefix example
        ]
    ]
}
```

**storagePath** config param is not required, but it is useful to version your assets automatically, so that you don't have to handle cache invalidation.

**gzip** config param default is **false**, only original compiled files are uploaded.
If **gzip** is set to **true**, it will upload compressed compiled files.
If **gzip** is set to **both**, it will upload original compiled files + compressed compiled files (with .gz extension).

You should set a pretty big **expires** value (to add **Cache-Control** and **Expires** metadata), so that browsers cache assets locally.

Note: for providers credentials, never use your root user access keys, you should create a specific user (ex. AWS IAM user) with the corresponding bucket permissions.


# Usage

## Pushing your assets to a Cloud Storage Service

Add this command to your build process (usually before war generation and deployment).

```groovy
gradle assetCompile uploadAssets
```

## Using your CDN-based assets

In your [Asset Pipeline](https://github.com/bertramdev/asset-pipeline) config, add your CDN URL (including your app prefix)

```groovy
grails.assets.url = "https://s3.amazonaws.com/my-bucket/assets/${project.name}-${project.version}"
```

# Latest releases

* 2015-08-23 **V0.1.1** : Initial release

# Bugs

To report any bug, please use the project [Issues](http://github.com/agorapulse/gradle-asset-pipeline-cdn/issues) section on GitHub.