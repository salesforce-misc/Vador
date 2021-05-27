# 🦾 Vader 🦾

[![Build Status](https://ccspaymentsci.dop.sfdc.net/buildStatus/icon?job=validation%2FVader%2Fmaster)](https://ccspaymentsci.dop.sfdc.net/job/validation/job/Vader/job/master/)
[![Version](https://img.shields.io/maven-metadata/v?label=nexus.soma&metadataUrl=https%3A%2F%2Fnexus.soma.salesforce.com%2Fnexus%2Fcontent%2Frepositories%2Freleases%2Fcom%2Fsalesforce%2Fccspayments%2Fvader%2Fmaven-metadata.xml)](https://nexus.soma.salesforce.com/nexus/content/repositories/releases/com/salesforce/ccspayments/vader/)
[![Quality Gate Status](https://sonarqube.soma.salesforce.com/api/project_badges/measure?project=ccspayments.vader&metric=alert_status)](https://sonarqube.soma.salesforce.com/dashboard?id=ccspayments.vader)
[![Maintainability Rating](https://sonarqube.soma.salesforce.com/api/project_badges/measure?project=ccspayments.vader&metric=sqale_rating)](https://sonarqube.soma.salesforce.com/dashboard?id=ccspayments.vader)
[![Security Rating](https://sonarqube.soma.salesforce.com/api/project_badges/measure?project=ccspayments.vader&metric=security_rating)](https://sonarqube.soma.salesforce.com/dashboard?id=ccspayments.vader)
[![Coverage](https://sonarqube.soma.salesforce.com/api/project_badges/measure?project=ccspayments.vader&metric=coverage)](https://sonarqube.soma.salesforce.com/dashboard?id=ccspayments.vader)
[![Slack](https://img.shields.io/badge/QTC-Centaurus-eng.svg?logo=slack)](https://sfdc-salescloud.slack.com/messages/TFBAFAVPH/team/)

🚧 ![stability-wip](https://img.shields.io/badge/stability-work_in_progress-lightgrey.svg)

![inline](images/vader.png)

> A piece-of-code is a costly solution to solve a simple problem - Lord Vader

---

# Mission Statement

> **Abstract away the Validation Orchestration complexity with a Reusable Design, that sets a framework to Write & Wire validations that are easy to Test, Extend and Share**

---

## Why a new Framework for Bean validation?

When shopping for Bean validation frameworks, we stumbled-upon these well-known ones:

- [Java Bean validation](https://www.baeldung.com/javax-validation): This is only suitable for
  simple data validations like `@NotNull, @Min, @Max`.
- [Spring Bean validation](https://reflectoring.io/bean-validation-with-spring-boot/): It comes with
  a lot of *Spring-baggage* and works with Spring REST. But we predominantly use Connect framework
  on core.

## Problem with `@Annotation` based validators

Annotations are reflection based, and they create a lot of *runtime magic*. They are not bad
in-general, but using them for validations has these cons:

- It's difficult to debug as you wouldn't know which `AnnotationProcessor` handles
  which `@Annotation` unless the Javadoc writer of that Annotation is gracious to provide those
  details.
- You can't use a simple *⌘+Click* to know what's going on underneath anymore.
- Annotations offer limited type-safety. It’s not possible to specify contextual requirements. Any
  annotation can go any type.
- Use of Reflections for Annotations also incur a runtime cost.
- Annotations are not testable.

Let's understand what kind of validations can services that belong to the same domain have.

## Service validations that belong to a Domain

We have a group of services under Payments-Platform domain, such as - Authorization, Capture,
Refund, Void. Similar service groups exist in Tax, Billing, Invoice domains too. All of these are
REST-APIs that accept JSON payload. Services that support *batch* accept list of JSON sub-requests.
A simplified version of a batch payload looks like this:

```jsonc
[
    {
        "amount": 99,
        "accountId": "{{validAccountId}}",
        ...,
        "paymentMethod": {
            ...
        },
        ...
    },
    {
        "amount": 77,
        "accountId": "{{validAccountId}}",
        ...,
        "paymentMethod": {
            ...
        },
        ...
    }
]
```

This JSON structure gets marshaled into a *Bean/POJO*, which needs to be validated at the entry
point of our application layer. Since all services in this domain deal with similar fields, they
have a lot of common fields like `amount, accountId` etc., as well as common member nodes
like `paymentMethod` in their structure. Based on the type of field, there exist 4 kinds of
validations. E.g.:

- _Data validations_ - to validate data integrity for fields like `amount`.
- _Effectful validations_ - for fields like `accountId`, which involves a DB read to verify.
- _Common Validations_ - for common fields that exist across services, such as `amount`, `accountId`
  .
- _Nested Validations_ - for the member nodes like `paymentMethod` . These nested members share an
  Aggregation/Composition relationship with their container and have validations of their own. A
  service in the same domain may reuse this data-struture in its payload. Such service, along with
  it's own validations, needs to execute all the validations of this nested member.

### Requirements for Validation Orchestration

Now that we talked about types of validations, let's understand the requirements for validation
orchestration (how to execute these validations).

- **Share Validations:** Instead of rewriting, Share Common and Nested Validations among services
  that share payload structure.
- **2 Routes - 2 execution Strategies:** Our database entities can be CRUD through two routes
  i.e., **Connect** and **SObject**. They both need to be guarded with Validations. But the tricky
  part is - the Connect route needs to *fail-fast*, while the SObject needs *error-accumulation*.
- **Configure Validation Order for Fail-fast:** Way to configure Cheaper validations first and
  Costlier later. Costlier validations can include Effectful validations, so we need to fail-fast
  and avoid unnecessary DB calls.
- **Partial failures for Batch APIs:**  An aggregated error response for failed sub-requests can
  only be sent after valid requests are processed through multiple layers of the application. We
  have to hold on to the invalid sub-requests till the end and skip them from processing.
- **Meta-requirements:**
    - Accommodate a century of validations across a domain
    - Unit testability for Validations
    - No compromise on Performance

> And so, **Vader** is born!!
>
> 🔊 [The birth of Lord Vader](https://www.youtube.com/watch?v=49WFdDIFlAs) playing in the background

![inline](images/birth-of-vader.gif)

---

# Why Vader?

On core, validation orchestration is predominantly done with `if-else-try-catch` pyramids, similar
to [this]([railway-oriented-validation/ImperativeValidation.java at master · overfullstack/railway-oriented-validation (github.com)](https://github.com/overfullstack/railway-oriented-validation/blob/master/src/main/java/app/imperative/ImperativeValidation.java))
. A domain may have more than 60 validations across its batch & non-batch services. Having
validations as loose functions for the above requirements, can create a mess of function calls:

![inline](images/function-call-mess.png)

This approach can spike the [**Cyclomatic
Complexity**](https://www.ibm.com/developerworks/java/library/j-cq03316/) and [**Cognitive
Complexity**] (https://www.sonarsource.com/docs/CognitiveComplexity.pdf) metrics and renders a
code-base which is difficult to test, extend and maintain.

### The 3D design problem

This problem is a 3-dimensional design problem stretching among - Sub-requests, Service routes (
sharing common fields & nodes), and Validation count. In the above imperative approach, we entangled
all 3, which lead to chaos. We need a design, which treats all of these separately, let them extend
independently, and abstracts out validation sequencing and orchestration. **We need to separate *
What-to-do* from *How-to-do.***

### Prove it!

Watch this Tech-talk as a prerequisite to understanding, why `if-else-try-catch` is easy to start
but difficult to manage and how Vader disciplines your code-base:

Talk-Proposal: https://overfullstack.ga/posts/fight-complexity-with-fp/

Recording: [Fight Complexity with Functional Programming - Gopal S. Akshintala - All Things Open, USA, 2020](https://www.youtube.com/watch?v=Dvr6gx4XaD8&list=PLrJbJ9wDl9EC0bG6y9fyDylcfmB_lT_Or)

---

### More videos

These recordings are initial presentations for Vader within the team.

> ⚠️ The Design has evolved after these recordings, but Vader's philosophy remains the same. Follow the rest of this doc to know more

[Brown Bag Session](https://drive.google.com/open?id=1AciJ3xU5HFgeTwJxL0RME0mQVO08BMQ9)

[An Internal Presentation](https://drive.google.com/open?id=1Syi3smlcyFAL0ZoDuq5dWR1IALZNmNUm) (This
is partially recorded)

---

# TL;DR Show me the code

### [billing-services](https://codesearch.data.sfdc.net/source/xref/app_main_core/app/main/core/billing-services/java/src/core/billing/service/billingschedule/config/BillingScheduleConfig.java#166)

---

# 🤩 [Specs](docs/specs.md) (New in 2.0)

---

# What is Vader?

**Vader is an independent Bean validation framework, not tied to any REST framework**. It's agnostic
of its consumer's Rest framework, Request data-type or failure representation. Although it's written
with the context explained above, its implementation is generic and can cater to anyone looking for
a declarative way to validate their Beans/POJOs.

Vader follows **Functional Programming** philosophy to segregate *What-to-do* from *How-to-do*. The
framework needs your validations to be broken into 3 ***decoupled*** parts:

- Validations *(What-to-do)* - Write your validations as First-Class Functions.
- Configuration *(How-to-do)* - Stitch your validations together into a Chain.
- Orchestration *(How-to-do)* - Call the API function as per the execution strategy (Fail-Fast or
  Error-Accumulation)

Let's deep dive into how Vader can help you in each of these steps:

---

# ƛ Data-types for Lambda

The job of validator is simple, just to convey if a POJO is valid or why it's invalid (in the form
of a Validation Failure).

Vader provides various **Validator Data-Types**, to get this done. These
are [Functional Interfaces](https://www.baeldung.com/java-8-functional-interfaces) to which a lambda
can be assigned.

## Validator `(ValidatableT) -> FailureT`

The Data type for simple first-class functions. It takes in a bean to be validated, represented
by `ValidatableT`, and returns a failure `FailureT`. This is prefixed *Simple* as it works with
Simple types as Input/Output.

```java
public static final Validator<Container, ValidationFailure> validation1=
        containerInputRepresentation->{
        if(containerInputRepresentation._isSetPaymentAuthorizationId()){
        return null;
        }else{
        return new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING,FIELD_NULL_OR_EMPTY,
        ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
        }
        };
```

### If you need more ⚡️Power⚡️

![inline](images/more-power.gif)

## ValidatorEtr (`Either<FailureT, ValidatableT>) -> Either<FailureT, ?>`

### Either Monad

Unlike Simple validator types (which work with Simple input/output types), these Data types work
with `Either` types as input/output. The `Either` type is borrowed
from [Vavr](https://docs.vavr.io/#_either).

### What's so powerful about `Either`?

With `Either`, You get all the functional programming powers. You can write linear programs with a
lot less **Cyclomatic Complexity** & **Cognitive Complexity**.

Please refer to this tech talk discussing these
concepts: [Fight Complexity with Functional Programming - Gopal S. Akshintala - All Things Open, USA, 2020](https://www.youtube.com/watch?v=Dvr6gx4XaD8&list=PLrJbJ9wDl9EC0bG6y9fyDylcfmB_lT_Or)

Lambdas assigned to `ValidatorEtr` take `Either<FailureT, ValidatableT>` as input. Since the input
is pre-wrapped in an `Either`, you can perform all the `Either` operations on the input like `map`
, `flatMap`, `fold`, `filterOrElse` (
Refer [API](https://www.javadoc.io/doc/io.vavr/vavr/0.10.2/io/vavr/control/Either.html) for more
info).

The result of the function is supposed to be `Either<FailureT, ?>`. This signifies, if there is a
Validation Failure, keep it in the *left* state. If the `Either` in the result is in *right* state,
it is considered that the bean **Passed** the validation. The wildcard `?` signifies it doesn't
matter what is the value in the right state.

```java
public static final ValidatorEtr<Container, ValidationFailure> batchValidation1=
        containerInputRepresentation->containerInputRepresentation
        .filterOrElse(Container::_isSetAccountId,ignore->new ValidationFailure(
        ApiErrorCodes.REQUIRED_FIELD_MISSING,FIELD_NULL_OR_EMPTY,
        ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID));
```

⚠️ Of-course, pre-wrapping into `Either` is just to avoid boiler-plate. You can very well
use `Validator` and wrap/unwrap it yourself.

## `Validator` vs `ValidatorEtr`?

These types only differ stylistically. They are there to help developers focus only on their
validation logic, not worry about boiler-plate and use a programming style (imperative or
functional) that they are comfortable in. You can essentially use any Data type for your validators
and in-fact you can even have a mix, based on your needs. Vader leaves the choice to the developer.
If you are using a mix, there's a little *Stitching* job you need to perform, which we shall see in
the Util section.

---

# How to Stitch these Validators?

Validators for different beans can be written in their own java classes. Using lambdas, we
essentially **use functions as values**. So, all we need is an Ordered List (like `java.util.List`)
to maintain the sequence of validations. We can chain all the validators, in the order of
preference.

```java
List<ValidatorEtr<Container, ValidationFailure> validatorChain=List.of(validator1,validator2,...);
```

## How to combine Container & Member validations?

But there's a catch! A List of Validators for a container node consists of a mix of all container
validators and all its nested member validators. But they can't be put under one `List`, as they are
functions on different Data Types.

```java
ValidatorEtr<Container, ValidationFailure> containerValidator=... // Apply same anology for Validator
        ValidatorEtr<Member, ValidationFailure> memberValidator=...

        List.of(containerValidator,memberValidator); // ^^^ Compile Error
```

So all nested member validations need to be lifted to the container type, essentially changing their
type matching with the Container's, like: `ValidatorEtr<Container, ValidationFailure>`.

We can achieve this with **Higher-Order Functions**, which **lift** member validator to the
container type. This takes a `containerToMemberMapper` which is function to extract member from
container.

```java
ValidatorEtr<Member, ValidationFailure> memberValidator=...
        ValidatorEtr<Container, ValidationFailure> liftedMemberValidator=
        liftToContainerValidatorType(memberValidator,containerToMemberMapper)
```

**This is a powerful technique, which lets you validate any Bean with any level or nesting. It's
easy to fit this model in our heads, as validator configuration aligns with Bean
hierarchical-structure**

![inline](images/hierarchical-validation.png)

This way, we can configure a **Chain** of validators in-order, sorting out all the container-member
dependencies. This is nothing but the most popular **Chain of Responsibility** Design pattern, with
a functional touch.

If the inter-dependencies between Container-Member happens to be more complex, we may end-up with *
Graph* relationship, but we can easily *flatten* it into a Chain with a simple *Topological Sort*.

## How to combine SimpleValidators & Validators?

Similarly, Vader has utils to lift `Validator` to `ValidatorEtr`. This is handy, when you have a mix
of validations, and they all need to be of the same type to stitch them together.

## Lift Util

Below are the utils currently available, with self-explanatory names. There are multiple overloads
suitable for simple/non-simple. The Java Docs should guide you to use proper overload:

| Aggregation Util: To lift Member validator to Container validator type ||
| ---  | --- |
| liftToContainerValidatorType | liftAllToContainerValidatorType |

| Validator Util: To lift Simple validator to ValidatorEtr type || | --- | :-- | | liftSimple |
liftAllSimple |

## Deferred result

If you skim through the source code, you can realize none of these Util functions does any
execution. These Higher-Order functions simply take-in a function and return a lifted function,
deferring the actual execution until you call any API method in the *Runner* API below:

---

# Validation Execution Strategies

Now that we know how to write & wire validations, the last step to execute these validations is
to **call an execution method, passing this config as a parameter**.

This can be seen as the **Edge** for validation bounded context, where the actual execution of
validations happen, and you get back the final results. *The complexity of how these validators are
orchestrated per strategy is abstracted away from the consumer.*

**There are various flavors (Overloads) in the Runner for Batch/Non-Batch and Simple/Non-Simple (
Please refer to Java Docs)**.

### `validateAndFailFast(validatorChain, ...)`

### `validateAndAccumulateErrors(validatorChain,...)`

## What about exceptions?

These Runner methods accept a parameter called `throwableMapper (Throwable) -> FailureT`. If any of
your validations throws a checked or unchecked exception, it shall be mapped into a failure
representation using this throwableMapper function.

---

# Current State

- This idea was selected to be presented as a Tech-talk on
  many [International Conferences & Meetups](https://overfullstack.ga/posts/fight-complexity-with-fp/#My-Talk-on-this)
  .
- This is currently consumed in **Production** by Payments and Tax domains.
- In 232, we used this in Billing domain to refactor `BillingScheduleService` batch API.

## Future

- There is a [PTOn proposal for 2.0](https://salesforce.quip.com/yDPwAHqycAKX) with many features
  and enhancements.
- Context-aware validations to share data among validations

If you have specific requirement, please log a git.soma issue.

---

![inline](images/vader.gif)

---

# Source-code Setup

- This is a simple Gradle project and has its own Gradle wrapper. So nothing to install. As of
  writing this, Gradle v7.0 is used. You need to add your nexus credentials in
  `~/.gradle/gradle.properties` file, which are used by your local gradle to connect to nexus.

  ```properties
    nexusUsername=...
    nexusPassword=....
  ```

- For source code navigation you need to have **[Lombok](https://projectlombok.org/)** plugin, which
  is used to generate boilerplate code. There are plugins available for Lombok for all popular IDEs,
  which you need to install. The latest version of the plugin should work.
- It uses **[google-java-format](https://github.com/google/google-java-format)**. Your check-in
  fails if you don't adhere to this style. Please set up your IDE to follow this formatting,
  following
  instructions [here](https://github.com/HPI-Information-Systems/Metanome/wiki/Installing-the-google-styleguide-settings-in-intellij-and-eclipse)

---

# Artifact Coordinates

```xml

<dependency>
    <groupId>com.salesforce.ccspayments</groupId>
    <artifactId>vader</artifactId>
    <version>${revcloud.vader.version}</version>
</dependency>
```

---

# For Contributors

## CI/CD

- The CI/CD pipeline is all set up for this library. A CI/CD jenkins job runs for every commit push
  on any branch. However, to publish a jar to nexus, only commits on `master` branch are considered.
  You can get more information from `JenkinsFile` at the root of this repo.
- The job status can be monitored
  [here](https://ccspaymentsci.dop.sfdc.net/job/validation/job/Vader/job/master/)
  or wait for the build-sheild at the top to turn red/green.

## Manual

- If you want to manually publish jar to nexus from you local, you need to have the necessary
  permissions to release jar into Nexus for both SNAPSHOTS and RELEASE channels. This Gus item was
  raised for Nexus access - https://gus.my.salesforce.com/a07B0000007Qt0BIAS. Please reach out to
  this team for any Nexus related permissions, problems, or requirements.
- As of today, the permissions are maintained by providing a role `CCSPayments`. We are planning to
  create an AD group for this.
- Increment the version property `version = "<Version-Number>"` and run this command.

  ```shell
  ./gradlew publish
  ```
- After publishing, you can verify it by searching in
  the [Nexus Repo](https://nexus.soma.salesforce.com/nexus/index.html#welcome).
