package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class MapLabel {
    private String label;
    private Pixel centroid;
    private Pixel[] farthestPoints;
    private List<Pixel> bezierPoints;
    private static final BitmapFont font = new BitmapFont(Gdx.files.internal("ui/fonts/tahoma_60.fnt"), false);

    public MapLabel(String label, List<Pixel> borderPixels) {
        this.label = label;
        List<Pixel> convexHull = this.getConvexHull(borderPixels);
        this.centroid = this.getCentroid(convexHull);
        this.farthestPoints = this.findFarthestPoints(convexHull);
        this.bezierPoints = this.calculateQuadraticBezierCurve();
    }

    public Pixel getTexturePosition() {
        return new Pixel((short) Math.min(farthestPoints[0].getX(), farthestPoints[1].getX()), (short) Math.min(farthestPoints[0].getY(), farthestPoints[1].getY()));
    }

    private List<Pixel> getConvexHull(List<Pixel> borderPixels) {
        List<Pixel> approximateBorder = new ArrayList<>();
        int step = 5;
        Pixel minPixel = this.getMinPixel(borderPixels);
        Pixel maxPixel = this.getMaxPixel(borderPixels);

        for (int x = minPixel.getX(); x <= maxPixel.getX(); x += step) {
            Pixel minYPoint = this.getMinYPointInRange(borderPixels, x, x + step);
            Pixel maxYPoint = this.getMaxYPointInRange(borderPixels, x, x + step);

            if (minYPoint != null) approximateBorder.add(minYPoint);
            if (maxYPoint != null) approximateBorder.add(maxYPoint);
        }

        for (int y = minPixel.getY(); y <= maxPixel.getY(); y += step) {
            Pixel minXPoint = this.getMinXPointInRange(borderPixels, y, y + step);
            Pixel maxXPoint = this.getMaxXPointInRange(borderPixels, y, y + step);

            if (minXPoint != null) approximateBorder.add(minXPoint);
            if (maxXPoint != null) approximateBorder.add(maxXPoint);
        }

        return approximateBorder;
    }

    private Pixel getMinYPointInRange(List<Pixel> pixels, int xStart, int xEnd) {
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

    private Pixel getMaxYPointInRange(List<Pixel> pixels, int xStart, int xEnd) {
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

    private Pixel getMinXPointInRange(List<Pixel> pixels, int yStart, int yEnd) {
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

    private Pixel getMaxXPointInRange(List<Pixel> pixels, int yStart, int yEnd) {
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

    private Pixel getMinPixel(List<Pixel> pixels) {
        short xMin = Short.MAX_VALUE;
        short yMin = Short.MAX_VALUE;
        for(Pixel pixel : pixels) {
            if(pixel.getX() < xMin) {
                xMin = pixel.getX();
            }
            if(pixel.getY() < yMin) {
                yMin = pixel.getY();
            }
        }

        return new Pixel(xMin, yMin);
    }

    private Pixel getMaxPixel(List<Pixel> pixels) {
        short xMax = 0;
        short yMax = 0;
        for(Pixel pixel : pixels) {
            if(pixel.getX() > xMax) {
                xMax = pixel.getX();
            }
            if(pixel.getY() > yMax) {
                yMax = pixel.getY();
            }
        }

        return new Pixel(xMax, yMax);
    }

    private Pixel getCentroid(List<Pixel> pixels) {
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

    private Pixel[] findFarthestPoints(List<Pixel> pixels) {
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

    private List<Pixel> calculateQuadraticBezierCurve() {
        List<Pixel> points = new ArrayList<>();

        int numberOfPoints = this.label.length();

        for (int i = 0; i < numberOfPoints; i++) {
            float t = i / (float) numberOfPoints;
            float x = (1 - t) * (1 - t) * this.farthestPoints[0].getX() + 2 * (1 - t) * t * centroid.getX() + t * t * this.farthestPoints[1].getX();
            float y = (1 - t) * (1 - t) * this.farthestPoints[0].getY() + 2 * (1 - t) * t * centroid.getY() + t * t * this.farthestPoints[1].getY();

            points.add(new Pixel((short) x, (short) y));
        }

        return points;
    }
}
