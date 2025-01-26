# Project Base for Vaadin and Spring Boot

This project can be used as a starting point to create your own Vaadin application with Spring Boot.
It contains all the necessary configuration and some placeholder files to get you started.

The best way to create your own project based on this starter is [start.vaadin.com](https://start.vaadin.com/) - you can get only the necessary parts and choose the package naming you want to use.

## TODO

* Export / Import Database tests
* Ability to remove items from the watchlist
    * active/inactive 
* Integration tests
* Move Database to [Supabase](https://supabase.com/pricing)

## Native Image

* At 07-12-2024 Spring Framework 6.2.0 (with Sprint Boot 3.4.0) fails reflection for Native Image
  * can use Spring Boot 3.3.6
  * GitHub issues:
    * https://github.com/spring-projects/spring-framework/issues/33936
    * https://github.com/spring-projects/spring-framework/pull/33950 (fix for 6.2.1)
* https://www.graalvm.org/latest/reference-manual/native-image/guides/build-spring-boot-app-into-native-executable/
* https://vaadin.com/docs/latest/flow/production/native


```shell
sdk install java 23.0.1-graal
```

```shell
mvn -Pproduction -Pnative native:compile
```

### Doesnt Work

```shell
2024-12-07T14:11:38.633+01:00  WARN 52773 --- [where-was-i] [           main] w.s.c.ServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'watchlistServiceImpl': Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'userServiceImpl': Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'flyway': Instantiation of supplied bean failed
2024-12-07T14:11:38.633+01:00  INFO 52773 --- [where-was-i] [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
Application run failed
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'watchlistServiceImpl': Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'userServiceImpl': Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'flyway': Instantiation of supplied bean failed
        at org.springframework.beans.factory.aot.BeanInstanceSupplier.resolveAutowiredArgument(BeanInstanceSupplier.java:345)
```

## Running the Application
There are two ways to run the application :  using `mvn spring-boot:run` or by running the `Application` class directly from your IDE.

You can use any IDE of your preference,but we suggest Eclipse or Intellij IDEA.
Below are the configuration details to start the project using a `spring-boot:run` command. Both Eclipse and Intellij IDEA are covered.

### Docker

```shell
export VERSION=0.1.0
```

```shell
make docker
```

```shell
docker run -i --rm \
  --name where-was-i \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e JDBC_DATABASE_URL=jdbc:postgresql://172.19.0.1:6543/wherewasi \
  ghcr.io/joostvdg/where-was-i:$VERSION
```

#### Eclipse
- Right click on a project folder and select `Run As` --> `Maven build..` . After that a configuration window is opened.
- In the window set the value of the **Goals** field to `spring-boot:run` 
- You can optionally select `Skip tests` checkbox
- All the other settings can be left to default

Once configurations are set clicking `Run` will start the application

#### Intellij IDEA
- On the right side of the window, select Maven --> Plugins--> `spring-boot` --> `spring-boot:run` goal
- Optionally, you can disable tests by clicking on a `Skip Tests mode` blue button.

Clicking on the green run button will start the application.

After the application has started, you can view your it at http://localhost:8080/ in your browser.


If you want to run the application locally in the production mode, use `spring-boot:run -Pproduction` command instead.

### Running Integration Tests

Integration tests are implemented using [Vaadin TestBench](https://vaadin.com/testbench). The tests take a few minutes to run and are therefore included in a separate Maven profile. We recommend running tests with a production build to minimize the chance of development time toolchains affecting test stability. To run the tests using Google Chrome, execute

`mvn verify -Pit,production`

and make sure you have a valid TestBench license installed.

## Structure

Vaadin web applications are full-stack and include both client-side and server-side code in the same project.

| Directory                                  | Description |
|:-------------------------------------------| :--- |
| `src/main/frontend/`                       | Client-side source directory |
| &nbsp;&nbsp;&nbsp;&nbsp;`index.html`       | HTML template |
| &nbsp;&nbsp;&nbsp;&nbsp;`index.ts`         | Frontend entrypoint |
| &nbsp;&nbsp;&nbsp;&nbsp;`main-layout.ts`   | Main layout Web Component (optional) |
| &nbsp;&nbsp;&nbsp;&nbsp;`views/`           | UI views Web Components (TypeScript / HTML) |
| &nbsp;&nbsp;&nbsp;&nbsp;`styles/`          | Styles directory (CSS) |
| `src/main/java/<groupId>/`                 | Server-side source directory |
| &nbsp;&nbsp;&nbsp;&nbsp;`Application.java` | Server entrypoint |
| &nbsp;&nbsp;&nbsp;&nbsp;`AppShell.java`    | application-shell configuration |


## GitHub Principal Attributes

* login: joostvdg
* id: 539630
* name: Joost van der Griendt

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorials at [vaadin.com/tutorials](https://vaadin.com/tutorials).
- Watch training videos and get certified at [vaadin.com/learn/training](https://vaadin.com/learn/training).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/components](https://vaadin.com/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Discover Vaadin's set of CSS utility classes that enable building any UI without custom CSS in the [docs](https://vaadin.com/docs/latest/ds/foundation/utility-classes). 
- Find a collection of solutions to common use cases in [Vaadin Cookbook](https://cookbook.vaadin.com/).
- Find Add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin/platform).
