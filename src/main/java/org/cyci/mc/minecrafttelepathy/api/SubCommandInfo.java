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
public @interface SubCommandInfo {
    String name();
    String[] args() default {};
    String description() default "";
    String usage() default "";
    String permission() default "";
}