package com.populaire.projetguerrefroide.map;

import java.util.ArrayList;
import java.util.List;

public class MapLabel {

    private Pixel centroid;
    private Pixel[] farthestPoints;
    //attribute convexHull is temporar to test the convexHull method
    private List<Pixel> convexHull;
    public MapLabel(List<Pixel> borderPixels) {
        List<Pixel> convexHull = getConvexHull(borderPixels);
        this.convexHull = convexHull;
        this.centroid = getCentroid(convexHull);
        this.farthestPoints = findFarthestPoints(convexHull);
        System.out.println("Centroid: " + centroid);
        System.out.println("Farthest points: " + farthestPoints[0] + " " + farthestPoints[1]);
    }

    public Pixel getCentroid() {
        return centroid;
    }

    public Pixel[] getFarthestPoints() {
        return farthestPoints;
    }

    public List<Pixel> getConvexHull() {
        return convexHull;
    }


    public List<Pixel> getConvexHull(List<Pixel> borderPixels) {
        List<Pixel> approximateBorder = new ArrayList<>();
        int step = 5;

        for (int x = getMinX(borderPixels); x <= getMaxX(borderPixels); x += step) {
            Pixel minYPoint = getMinYPointInRange(borderPixels, x, x + step);
            Pixel maxYPoint = getMaxYPointInRange(borderPixels, x, x + step);

            if (minYPoint != null) approximateBorder.add(minYPoint);
            if (maxYPoint != null) approximateBorder.add(maxYPoint);
        }

        for (int y = getMinY(borderPixels); y <= getMaxY(borderPixels); y += step) {
            Pixel minXPoint = getMinXPointInRange(borderPixels, y, y + step);
            Pixel maxXPoint = getMaxXPointInRange(borderPixels, y, y + step);

            if (minXPoint != null) approximateBorder.add(minXPoint);
            if (maxXPoint != null) approximateBorder.add(maxXPoint);
        }

        return approximateBorder;
    }

    public Pixel getMinYPointInRange(List<Pixel> pixels, int xStart, int xEnd) {
        Pixel minYPoint = null;
        for (Pixel pixel : pixels) {
            if (pixel.getX() >= xStart && pixel.getX() < xEnd) {
                if (minYPoint == null || pixel.getY() < minYPoint.getY()) {
                    minYPoint = pixel;
                }
            }
        }
        return minYPoint;
    }

    public Pixel getMaxYPointInRange(List<Pixel> pixels, int xStart, int xEnd) {
        Pixel maxYPoint = null;
        for (Pixel pixel : pixels) {
            if (pixel.getX() >= xStart && pixel.getX() < xEnd) {
                if (maxYPoint == null || pixel.getY() > maxYPoint.getY()) {
                    maxYPoint = pixel;
                }
            }
        }
        return maxYPoint;
    }

    public Pixel getMinXPointInRange(List<Pixel> pixels, int yStart, int yEnd) {
        Pixel minXPoint = null;
        for (Pixel pixel : pixels) {
            if (pixel.getY() >= yStart && pixel.getY() < yEnd) {
                if (minXPoint == null || pixel.getX() < minXPoint.getX()) {
                    minXPoint = pixel;
                }
            }
        }
        return minXPoint;
    }

    public Pixel getMaxXPointInRange(List<Pixel> pixels, int yStart, int yEnd) {
        Pixel maxXPoint = null;
        for (Pixel pixel : pixels) {
            if (pixel.getY() >= yStart && pixel.getY() < yEnd) {
                if (maxXPoint == null || pixel.getX() > maxXPoint.getX()) {
                    maxXPoint = pixel;
                }
            }
        }
        return maxXPoint;
    }


    public int getMinX(List<Pixel> pixels) {
        return pixels.stream().mapToInt(Pixel::getX).min().orElse(0);
    }

    public int getMaxX(List<Pixel> pixels) {
        return pixels.stream().mapToInt(Pixel::getX).max().orElse(0);
    }

    public int getMinY(List<Pixel> pixels) {
        return pixels.stream().mapToInt(Pixel::getY).min().orElse(0);
    }

    public int getMaxY(List<Pixel> pixels) {
        return pixels.stream().mapToInt(Pixel::getY).max().orElse(0);
    }

    public Pixel getCentroid(List<Pixel> pixels) {
        if (pixels.isEmpty()) {
            return null;
        }

        int sumX = 0;
        int sumY = 0;
        for (Pixel pixel : pixels) {
            sumX += pixel.getX();
            sumY += pixel.getY();
        }
        int centerX = sumX / pixels.size();
        int centerY = sumY / pixels.size();
        return new Pixel((short) centerX, (short) centerY);
    }

    public Pixel[] findFarthestPoints(List<Pixel> pixels) {
        double maxDistance = 0;
        Pixel farthestPoint1 = null;
        Pixel farthestPoint2 = null;

        for (int i = 0; i < pixels.size(); i++) {
            for (int j = i + 1; j < pixels.size(); j++) {
                Pixel p1 = pixels.get(i);
                Pixel p2 = pixels.get(j);

                double distance = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));

                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestPoint1 = p1;
                    farthestPoint2 = p2;
                }
            }
        }

        if(farthestPoint1 == null) {
            return null;
        }

        return new Pixel[]{
            new Pixel(farthestPoint1.getX(), farthestPoint1.getY()),
            new Pixel(farthestPoint2.getX(), farthestPoint2.getY())
        };
    }
}
