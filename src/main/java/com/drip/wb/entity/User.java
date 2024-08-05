package com.drip.wb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String avatar;
    private String room;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!username.equals(user.username)) return false;
        return avatar.equals(user.avatar);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + avatar.hashCode();
        return result;
    }
}