package com.github.ibanetchep.msquests.core.util.testmodels;

import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;

@AtLeastOneOfFields({"foo", "bar"})
@AtLeastOneOfFields({"title", "title_key"})
public class DummyMultiGroupConfig {

    @ConfigField(name = "foo")
    private String foo;

    @ConfigField(name = "bar")
    private Integer bar;

    @ConfigField(name = "baz", required = true)
    private boolean baz;

    @ConfigField(name = "title")
    private String title;

    @ConfigField(name = "title_key")
    private String titleKey;
}
