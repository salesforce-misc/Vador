/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.types

import io.vavr.CheckedFunction1
import io.vavr.control.Either

fun interface Validator<ValidatableT, FailureT> : CheckedFunction1<ValidatableT, FailureT>

fun interface ValidatorEtr<ValidatableT, FailureT> :
  CheckedFunction1<Either<FailureT?, ValidatableT?>, Either<FailureT?, *>>
