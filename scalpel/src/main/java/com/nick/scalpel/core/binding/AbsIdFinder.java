/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nick.scalpel.core.binding;

import android.content.Context;

import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.utils.ReflectionUtils;

import java.lang.reflect.Field;

import static com.nick.scalpel.core.utils.ReflectionUtils.getField;
import static com.nick.scalpel.core.utils.ReflectionUtils.makeAccessible;

abstract class AbsIdFinder extends AbsContextedFinder {

    public AbsIdFinder(Configuration configuration) {
        super(configuration);
    }

    protected abstract int getIdRes(Field field);

    @Override
    public final void wire(Context context, Object object, Field field) {
        makeAccessible(field);
        Object fieldObject = getField(field, object);
        if (fieldObject != null && !ReflectionUtils.isBaseDataType(field.getType())) return;
        int id = getIdRes(field);
        if (id <= 0) invalidArg();
        wireFromId(id, context, object, field);
    }

    protected abstract void wireFromId(int id, Context context, Object object, Field field);
}
