/*
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package at.sciencesoft.oxrmi;

/**
 *
 * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
 */
public enum Param {

    NAME(TYPE.STRING),
    DISPLAY_NAME(TYPE.STRING),
    EMAIL(TYPE.STRING),
    EMAIL_DOMAIN(TYPE.STRINGLIST),
    ADD_USER(TYPE.STRINGLIST),
    REMOVE_USER(TYPE.STRINGLIST),
    ALL_USER(TYPE.STRINGLIST),
    MAPPING(TYPE.STRINGLIST),
    DESCRIPTION(TYPE.STRING),
    LOGIN_NAME(TYPE.STRING),
    PASSWORD(TYPE.STRING),
    FIRST_NAME(TYPE.STRING),
    LAST_NAME(TYPE.STRING),
    LANGUAGE(TYPE.STRING),
    COMPANY(TYPE.STRING),
    DEPARTMENT(TYPE.STRING),
    EMAIL_ALIAS(TYPE.STRINGLIST),
    IMAP_LOGIN(TYPE.STRING),
    SMTP_SERVER(TYPE.STRING),
    IMAP_SERVER(TYPE.STRING),
    TIME_ZONE(TYPE.STRING),
    UPLOAD_FILE_SIZE(TYPE.INT),
    UPLOAD_FILE_SIZE_PER_FILE(TYPE.INT),
    FILE_QUOTA(TYPE.INT),
    MAIL_QUOTA(TYPE.INT),
    ACCESS_COMBINATION(TYPE.STRING),
    EMAIL_FORWRAD(TYPE.INT),
    EMAIL_FORWRAD_LIST(TYPE.STRINGLIST),
    AUTORESPONDER(TYPE.INT),
    AUTORESPONDER_TEXT(TYPE.STRING),
    INTERVALL(TYPE.STRING),
    EMAIL_GROUP(TYPE.STRING),
    EMAIL_GROUP_ACTIVE(TYPE.BOOLEAN),
    EMAIL_GROUP_ADDITIONAL(TYPE.STRING),
    MIDDLE_NAME(TYPE.STRING),
    STREET_HOME(TYPE.STRING),
    POSTAL_CODE_HOME(TYPE.STRING),
    CITY_HOME(TYPE.STRING),
    STATE_HOME(TYPE.STRING),
    STREET_BUSINESS(TYPE.STRING),
    POSTAL_CODE_BUSINESS(TYPE.STRING),
    CITY_BUSINESS(TYPE.STRING),
    STATE_BUSINESS(TYPE.STRING),
    TELEPHONE_BUSINESS(TYPE.STRING),
    FAX_BUSINESS(TYPE.STRING),
    TELEPHONE_HOME(TYPE.STRING),
    MOBILE(TYPE.STRING);

    public enum TYPE {

        STRING, STRINGLIST, INT, BOOLEAN
    };

    Param(TYPE type) {
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }
    private TYPE type;
}
