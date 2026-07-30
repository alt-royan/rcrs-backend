package org.ultra.rcrs.libraryservice.model;

/**
 * Audio quality of a downloaded file. Mirrors media-service's own {@code Quality}
 * enum by name; library-service only records which one the user has on the device
 * and never resolves it to an actual asset.
 */
public enum Quality {
    LOW,
    MID,
    HIGH
}
