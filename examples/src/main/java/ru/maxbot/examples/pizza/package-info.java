/**
 * PizzaBot example package.
 *
 * <p>Shows a more complete bot structure instead of isolated demo handlers:</p>
 * <ul>
 *   <li>main menu and customer navigation</li>
 *   <li>multi-step order flow with states and callbacks</li>
 *   <li>customer profile and staff access commands</li>
 *   <li>staff-only kitchen and statistics commands via Spring Security</li>
 *   <li>interceptors and global/local exception handling in a realistic scenario</li>
 * </ul>
 *
 * <p>Run with {@code MAX_BOT_TOKEN=... mvn -pl examples spring-boot:run}.
 * The shared {@code ExampleApplication} scans this package automatically.</p>
 */
package ru.maxbot.examples.pizza;
