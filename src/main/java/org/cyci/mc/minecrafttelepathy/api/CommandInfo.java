package org.cyci.mc.minecrafttelepathy.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * org.cyci.mc.minecrafttelepathy.api
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String[] aliases() default {};
    String description() default "";
    String usage() default "";
    String[] args() default {};
    String permission() default "";
}
