package org.codehaus.mojo.versions.utils;

import org.apache.maven.model.Dependency;

import java.util.Set;
import java.util.TreeSet;

public class DependencyHelper {

    private DependencyHelper() {

    }

    /**
     * Filters the set of dependencies by the includes property.
     *
     * @return a new Set of dependencies filtered by includes, or the original Set if no includes was specified.
     */
    public static Set<Dependency> filterIncludes(Set<Dependency> dependencies, String includes) {
        Set<Dependency> filteredDependencies = new TreeSet<>(new DependencyComparator());

        if (includes == null) {
            return dependencies;
        }

        for (Dependency dependency : dependencies) {

            String[] tokens = new String[]{
                    dependency.getGroupId(),
                    dependency.getArtifactId(),
            };

            String[] patterns = includes.split(",");

            for (String pattern : patterns) {
                boolean matched = true;
                String[] patternTokens = pattern.split(":");

                for (int count = 0; count < patternTokens.length; count++) {
                    matched = matched && matches(tokens[count], patternTokens[count]);
                }

                if (matched) {
                    filteredDependencies.add(dependency);
                }
            }
        }
        return filteredDependencies;
    }


    /**
     * Gets whether the specified token matches the specified pattern segment.
     *
     * @param token   the token to check
     * @param pattern the pattern segment to match, as defined above
     * @return <code>true</code> if the specified token is matched by the specified pattern segment
     */
    public static boolean matches(String token, String pattern) {
        boolean matches;

        // support full wildcard and implied wildcard
        if ("*".equals(pattern) || pattern.length() == 0) {
            matches = true;
        }
        // support contains wildcard
        else if (pattern.startsWith("*") && pattern.endsWith("*")) {
            String contains = pattern.substring(1, pattern.length() - 1);

            matches = token.contains(contains);
        }
        // support leading wildcard
        else if (pattern.startsWith("*")) {
            String suffix = pattern.substring(1);

            matches = token.endsWith(suffix);
        }
        // support trailing wildcard
        else if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);

            matches = token.startsWith(prefix);
        } else {
            matches = token.equals(pattern);
        }

        return matches;
    }
}
