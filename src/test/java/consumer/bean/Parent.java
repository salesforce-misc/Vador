/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.bean;

import lombok.Value;


/**
 * Connect API input bean.
 *
 * @author gakshintala
 * @since 228
 */
@Value
public class Parent {
    Child child;
}
