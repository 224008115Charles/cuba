/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.sys.navigation;

import com.haulmont.bali.util.URLEncodeUtils;
import com.haulmont.cuba.web.sys.WebUrlRouting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is intended for serializing entity ids to be used as URL param.
 * <p>
 * String, Integer and Long ids are serialized as-is.
 * <p>
 * UUID ids are serialized using Crockford Base32 encoding.
 *
 * @see CrockfordEncoder
 * @see WebUrlRouting
 * @see UrlChangeHandler
 */
public class UrlIdSerializer {

    private static final Logger log = LoggerFactory.getLogger(UrlIdSerializer.class);

    private static final String STRING_UUID_SPLIT_REGEX = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    private static final Pattern STRING_UUID_SPLIT_PATTERN = Pattern.compile(STRING_UUID_SPLIT_REGEX);

    @Nullable
    public static String serializeId(Object id) {
        if (id == null) {
            return null;
        }

        String serialized = null;
        Class<?> idClass = id.getClass();

        if (String.class == idClass
                || Integer.class == idClass
                || Long.class == idClass) {
            serialized = URLEncodeUtils.encodeUtf8(id.toString());

        } else if (UUID.class == idClass) {
            try {
                String stringUuid = ((UUID) id).toString()
                        .replaceAll("-", "");

                BigInteger biUuid = new BigInteger(stringUuid, 16);

                serialized = CrockfordEncoder.encode(biUuid)
                        .toLowerCase();
            } catch (Exception e) {
                log.info("An error occurred while serializing UUID id: {}", id, e);
            }
        } else {
            log.info("Unable to serialize id '{}' of type '{}'", id, idClass);
        }

        return serialized;
    }

    @Nullable
    public static Object deserializeId(Class idClass, String serializedId) {
        if (idClass == null || StringUtils.isEmpty(serializedId)) {
            return null;
        }

        Object deserialized = null;
        String decoded = URLEncodeUtils.decodeUtf8(serializedId);

        try {
            if (String.class == idClass) {
                deserialized = decoded;

            } else if (Integer.class == idClass) {
                deserialized = Integer.valueOf(decoded);

            } else if (Long.class == idClass) {
                deserialized = Long.valueOf(decoded);

            } else if (UUID.class == idClass) {
                String stringUuid = CrockfordEncoder.decode(serializedId.toUpperCase())
                        .toString(16);

                Matcher matcher = STRING_UUID_SPLIT_PATTERN.matcher(stringUuid);
                if (matcher.matches()) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        sb.append(matcher.group(i));
                        if (i < matcher.groupCount()) {
                            sb.append('-');
                        }
                    }

                    deserialized = UUID.fromString(sb.toString());
                }
            } else {
                log.info("Unable to deserialize base64 id '{}' of type '{}'", serializedId, idClass);
            }
        } catch (Exception e) {
            log.info("An error occurred while deserializing base64 id: '{}' of type '{}'", serializedId, idClass, e);
        }

        return deserialized;
    }
}
