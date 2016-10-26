package org.jenkinsci.plugins.displayurlapi;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * Generates URLs for well known UI locations for use in notifications (e.g. mailer, HipChat, Slack, IRC, etc)
 * Extensible to allow plugins to override common URLs (e.g. Blue Ocean or another future secondary UI)
 */
public abstract class DisplayURLProvider implements ExtensionPoint {

    /**
     * Returns the first {@link DisplayURLProvider} found
     * @return DisplayURLProvider
     */
    public static DisplayURLProvider get() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        ExtensionList<DisplayURLProvider> allProviders = jenkins.getExtensionList(DisplayURLProvider.class);
        DisplayURLProvider defaultDisplayURLProvider = Iterables.find(allProviders, new Predicate<DisplayURLProvider>() {
            @Override
            public boolean apply(DisplayURLProvider input) {
                return input.getClass().equals(ClassicDisplayURLProvider.class);
            }
        });
        Iterable<DisplayURLProvider> availableDisplayURLProviders = Iterables.filter(
                allProviders,
                Predicates.not(Predicates.equalTo(defaultDisplayURLProvider))
        );
        return Iterables.getFirst(availableDisplayURLProviders, defaultDisplayURLProvider);
    }

    /** Fully qualified URL for the Root display URL */
    public String getRoot() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not started");
        }
        String root = jenkins.getRootUrl();
        if (root == null) {
            root = "http://unconfigured-jenkins-location/";
        }
        return Util.encode(root);
    }

    /** Name of this display URL provider */
    public abstract String getName();

    /** Fully qualified URL for a Run */
    public abstract String getRunURL(Run<?, ?> run);

    /** Fully qualified URL for a page that displays changes for a project. */
    public abstract String getChangesURL(Run<?, ?> run);

    /** Fully qualified URL for a Jobs home */
    public abstract String getJobURL(Job<?, ?> project);

    /** Fully qualified URL to the test details page for a given test result */
    public abstract String getTestUrl(hudson.tasks.test.TestResult result);
}
