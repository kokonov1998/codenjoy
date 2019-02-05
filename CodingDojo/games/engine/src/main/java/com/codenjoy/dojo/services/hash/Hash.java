package com.codenjoy.dojo.services.hash;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class Hash {

    public static String md5(String string) {
        return DigestUtils.md5Hex(string.getBytes());
    }

    public static String getId(String email, String soul) {
        if (StringUtils.isEmpty(email)) {
            return email;
        }

        String encoded = xor(email, soul);
        encoded = shuffle(encoded, shuffleSoul(soul, true));
        return encode(encoded);
    }

    private static String shuffleSoul(String soul, boolean inverted) {
        soul = encode(md5(soul) + md5(soul + "qwe"));
        return inverted ? StringUtils.reverse(soul) : soul;
    }

    private static String encode(String encoded) {
        return ZBase32Encoder.encode(encoded.getBytes());
    }

    public static String getEmail(String id, String soul) {
        if (StringUtils.isEmpty(id)) {
            return id;
        }

        String data = decode(id);
        data = shuffle(data, shuffleSoul(soul, false));
        return xor(data, soul);
    }

    private static String decode(String id) {
        return new String(ZBase32Encoder.decode(id));
    }

    private static String xor(String email, String soul) {
        String h1 = md5(soul);
        String h2 = h1 + md5(h1);

        AtomicInteger i = new AtomicInteger();
        return email.chars()
                .map(ch -> ch ^ h1.codePointAt(i.addAndGet(h2.codePointAt(i.get() % h1.length())) % h1.length()))
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    private static String shuffle(String input, String soul) {
        List<Character> l = input.chars()
                .mapToObj(c -> new Character((char) c))
                .collect(toList());

        soul.chars().forEach(c -> {
            swap((c >>> 12) ^ c >> input.length(), c ^ (c >>> 3), l);
        });

        return l.stream().collect(StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
                .toString();
    }

    private static void swap(int n, int m, List<Character> l) {
        m = m % l.size();
        n = n % l.size();
        l.set(m, l.set(n, l.get(m)));
    }

    // TODO сделать метод получения хеша с испольованием соли
    public static String getCode(String email, String password) {
        return "" + Math.abs(email.hashCode()) + Math.abs(password.hashCode());
    }

    public static void main(String[] args) {
        String soul = "soul";

        String email = "apofig@gmail.com";
        String password = "apofig@gmail.com";
        String passwordHash = md5(password);
        String id = getId(email, soul);
        String code = getCode(email, passwordHash);

        System.out.println("email: " + email);
        System.out.println("id: " + id);
        System.out.println("password: " + password);
        System.out.println("password md5: " + passwordHash);
        System.out.println("code: " + code);

        System.out.println("---");
        System.out.printf("UPDATE players " +
                "SET password = '%s', code = '%s' " +
                "WHERE email = '%s';\n", passwordHash, code, email);
    }

}
