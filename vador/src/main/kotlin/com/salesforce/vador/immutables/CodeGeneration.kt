package com.salesforce.vador.immutables

import org.immutables.value.Value

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class AllowNulls

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Value.Style(
  typeImmutable = "*",
  typeAbstract = ["Abstract*"],
  builder = "toValidate",
  build = "prepare",
  toBuilder = "toBuilder",
  depluralize = true,
  depluralizeDictionary =
    [
      "withIdConfig:withIdConfigs",
      "withFieldConfig:withFieldConfigs",
      "withSpec:withSpecs",
      "withValidatorEtr:withValidatorEtrs",
      "findAndFilterDuplicatesConfig:findAndFilterDuplicatesConfigs",
      "withContainerValidatorEtr:withContainerValidatorEtrs",
      "withBatchMember:withBatchMembers",
    ],
  add = "*",
  put = "*",
  visibility = Value.Style.ImplementationVisibility.PUBLIC,
)
annotation class ConfigStyle

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Value.Style(
  typeImmutable = "*",
  typeAbstract = ["Abstract*"],
  builder = "check",
  build = "done",
  toBuilder = "toBuilder",
  depluralize = true,
  add = "*",
  put = "*",
  visibility = Value.Style.ImplementationVisibility.PUBLIC,
)
annotation class SpecStyle
