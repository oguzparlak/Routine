package com.oguzparlak.wakemeup.model;

/**
 * @author Oguz Parlak
 *         <p>
 *         Description goes here
 *         </p/
 **/

public class MatrixDistanceModel {

    private String destinationAddress;
    private String distance;
    private String duration;

    public MatrixDistanceModel(String destinationAddress, String distance, String duration) {
        this.destinationAddress = destinationAddress;
        this.distance = distance;
        this.duration = duration;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }
}
