/* OpenSyncro - A web-based enterprise application integration tool
 * Copyright (C) 2008 Smilehouse Oy, support@opensyncro.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

/*
 * Created on Apr 27, 2005
 */
package smilehouse.opensyncro.pipes.component;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import smilehouse.util.LabelResource;

public class CustomLabelResource extends LabelResource {
    ResourceBundle customLabels;
    LabelResource defaultLabels;
    Locale locale;

    public CustomLabelResource(ResourceBundle customLabels,
                               LabelResource defaultLabels,
                               Locale locale) {
        super(defaultLabels, locale.getLanguage());
        this.customLabels = customLabels;
        this.defaultLabels = defaultLabels;
        this.locale = locale;
    }

    public String getLabel(String key) {
        try {
            return customLabels.getString(key);
        } catch(MissingResourceException mre) {
            if(defaultLabels != null)
                return defaultLabels.getLabel(key);
            else
                return null;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLabel(String key, String lang) {
        throw new UnsupportedOperationException(
            "This is a custom LabelResource for a PipeComponent and unfortunately this method is not supported.");
    }
}