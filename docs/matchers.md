# Matchers

Vader provides few matchers out of the box to help write Specs. These matchers address
generic use-cases common among consumers. New matchers can be added as needed/requested. Currently, these are the available matchers:
- AnyMatchers
    - `anyOf`
    - `anyOfOrNull`
- DateMatchers
    - `isOnOrBeforeIfBothArePresent`
    - `isBeforeIfBothArePresent`
    - `isEqualToDayOfDate`
- IntMatchers
    - inRangeInclusive
