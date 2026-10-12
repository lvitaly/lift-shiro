# Lift Shiro

This is an initial version of integration between [Apache Shiro](http://shiro.apache.org/) and the [Lift Web framework](http://liftweb.net/). Specifically, this integration does not use Shiro's built-in `web.xml` resource filters to control access to URLs; it instead uses Lift's sitemap Locs. In addition, it also has a range of snippets for conditionally displaying content based on rules and permissions.

## Usage

First, add the SBT dependency:

```scala
libraryDependencies += "eu.getintheloop" %% "lift-shiro" % "0.0.9-SNAPSHOT"

resolvers ++= Seq(
  "apache.repo" at "https://repository.apache.org/content/repositories/snapshots/",
  "sonatype.repo" at "https://oss.sonatype.org/content/repositories/public/"
)
```

Next, add this to the `web.xml` within your project in order to initialize the Shiro context when your application receives a request. You need to add this above the Lift filter and filter mapping. Otherwise, login redirection will not work.

```xml
<listener>
  <listener-class>org.apache.shiro.web.env.EnvironmentLoaderListener</listener-class>
</listener>
<filter>
  <filter-name>ShiroFilter</filter-name>
  <filter-class>org.apache.shiro.web.servlet.ShiroFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>ShiroFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

Next, add some configuration to your `Boot.scala`:

```scala
class Boot {
  def boot {
    import shiro.Shiro
    import shiro.sitemap.Locs._
    Shiro.init()
    // ...
  }
}
```

By default, this uses the Shiro `IniSecurityManagerFactory`, meaning you need to supply a `shiro.ini` at the root of your classpath that uses the Shiro INI configuration style. If, however, you want to use another security manager, supply it to the `init()` method like so:

```scala
Shiro.init(new IniSecurityManagerFactory("classpath:shiro.ini"))
```

To use the integration in your sitemap to restrict access to resources, apply it to your `SiteMap` like so:

```scala
LiftRules.setSiteMap(SiteMap(List(
    Menu("Home") / "index" >> RequireAuthentication,
    Menu("Role Test") / "restricted" >> RequireAuthentication >> HasRole("admin"),
    Menu("Login") / "login" >> RequireNoAuthentication
    ) ::: Shiro.menus: _*
  ))
```

There are several `LocParam`s that you can use to augment your `Menu` structure. Specifically, you can use:

* `RequireAuthentication` - Ensures that users are authenticated, irrespective of roles or permissions
* `RequireNoAuthentication` - Ensures public access (read: guest subject)
* `RequireRemembered` - Ensures that this subject is authenticated or remembered
* `RequireNotRemembered` - Ensures that the subject is *not* authenticated or remembered
* `HasRole("rolename")` - Ensures that the user has a specific role
* `HasPermission("somePermission")` - Ensures that the user has a specific permission
* `LacksPermission("permissionName")` - Ensures that the user lacks a specific permission

In addition to using the integration in the `SiteMap`, you can also use it within your markup with the following snippet technique:

```xml
<lift:has_role name="someRole">
  Some content that is only available to users who have the "someRole" role assigned to them
</lift:has_role>
```

The snippets this integration wires in are the following (all are methods on subject/subjects):

* `has_role`
* `lacks_role`
* `has_permission`
* `lacks_permission`
* `has_any_roles`
* `is_guest`
* `is_user`
* `is_authenticated`
* `is_not_authenticated`

All use the `name` attribute, with the exception of `has_any_roles`, which takes the `roles` attribute containing a comma-delimited list of assigned roles.

## FAQ

* **Why did you bother making this?** MegaProtoUberTron was not really for me, as I don't typically want the user data stored within my application (i.e., I'm using AD, OpenDirectory, etc.).
* **In that case, why not just make the LDAP connection using `javax.naming`?** Because I'm lazy, and frankly, I don't want to have to maintain that code when someone else is already doing it (likely better than I would).
