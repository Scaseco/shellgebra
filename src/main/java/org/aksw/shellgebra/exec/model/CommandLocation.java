package org.aksw.shellgebra.exec.model;

/**
 * A command on a certain exec site.
 */
public record CommandLocation(String command, ExecSite execSite) {}
