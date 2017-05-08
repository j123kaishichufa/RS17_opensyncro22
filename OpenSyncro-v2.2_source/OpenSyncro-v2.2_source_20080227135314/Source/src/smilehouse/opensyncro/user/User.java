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

package smilehouse.opensyncro.user;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class User implements Serializable {

    /** identifier field */
    private String login;

    /** nullable persistent field */
    private String password;

    /** nullable persistent field */
    private String name;

    /** full constructor */
    public User(String login, String password, String name) {
        this.login = login;
        this.password = password;
        this.name = name;
    }

    /** default constructor */
    public User() {
        this.name=""; 
    }

    /** minimal constructor */
    public User(String login) {
        this();
        this.login = login;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return new ToStringBuilder(this).append("login", getLogin()).toString();
    }

    public boolean equals(Object other) {
        if(!(other instanceof User))
            return false;
        User castOther = (User) other;
        return new EqualsBuilder().append(this.getLogin(), castOther.getLogin()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getLogin()).toHashCode();
    }

}