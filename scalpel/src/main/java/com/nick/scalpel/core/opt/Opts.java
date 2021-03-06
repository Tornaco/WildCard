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

package com.nick.scalpel.core.opt;

import com.nick.scalpel.R;
import com.nick.scalpel.Scalpel;
import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.Publishable;

public class Opts implements Publishable {

    private Opts() {
        // Noop.
    }

    public static void publishTo(Scalpel scalpel) {
        new Opts().publish(scalpel);
    }

    @Override
    public void publish(Scalpel scalpel) {
        Configuration configuration = scalpel.getConfiguration();
        new AutoRecycleWirer(configuration, scalpel.getLifeCycleManager()).publish(scalpel);
        BeanFactory.init(scalpel.getApp(), configuration,
                R.xml.context_scalpel_internal, configuration.getBeanContextRes());
        BeanFactory.getInstance().publish(scalpel);
    }
}
