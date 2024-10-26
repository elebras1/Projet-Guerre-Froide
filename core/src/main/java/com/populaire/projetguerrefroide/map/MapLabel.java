package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapLabel {
    private String label;
    private Pixel centroid;
    private Pixel[] farthestPoints;
    private List<CurvePoint> points;
    private static final BitmapFont font = new BitmapFont(Gdx.files.internal("ui/fonts/trebuchet_45.fnt"), false);
    private float fontScale;
    private static class CurvePoint {
        Pixel center;
        Pixel origin;
        float angle;

        public CurvePoint() {
        }

    }

    public MapLabel(String label, List<Pixel> borderPixels) {
        this.label = label;
        List<Pixel> convexHull = this.getConvexHull(borderPixels);
        this.setCentroid(convexHull);
        this.findFarthestPoints(convexHull);
        this.calculateQuadraticBezierCurve();
        GlyphLayout layout = new GlyphLayout();
        this.setFontScale(layout);
        this.setPointsOrigin(layout);
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

    private void setCentroid(List<Pixel> pixels) {
        int sumX = 0;
        int sumY = 0;
        for (Pixel pixel : pixels) {
            sumX += pixel.getX();
            sumY += pixel.getY();
        }
        int centerX = sumX / pixels.size();
        int centerY = sumY / pixels.size();
        this.centroid = new Pixel((short) centerX, (short) centerY);
    }

    private void findFarthestPoints(List<Pixel> pixels) {
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

        this.farthestPoints = new Pixel[] {
            new Pixel(farthestPoint1.getX(), farthestPoint1.getY()),
            new Pixel(farthestPoint2.getX(), farthestPoint2.getY())
        };
    }

    private void setFontScale(GlyphLayout layout) {
        layout.setText(font, label);
        float textLength = layout.width;
        float curveLength = this.approximateCurveLength();
        this.fontScale = (curveLength / textLength) * 0.66f;
    }

    private void calculateQuadraticBezierCurve() {
        this.points = new ArrayList<>();

        int numberOfPoints = this.label.length() + 2;

        for (int i = 0; i < numberOfPoints; i++) {
            float t = i / (float) numberOfPoints;
            float x = (1 - t) * (1 - t) * this.farthestPoints[0].getX() + 2 * (1 - t) * t * centroid.getX() + t * t * this.farthestPoints[1].getX();
            float y = (1 - t) * (1 - t) * this.farthestPoints[0].getY() + 2 * (1 - t) * t * centroid.getY() + t * t * this.farthestPoints[1].getY();

            CurvePoint curvePoint = new CurvePoint();
            curvePoint.center = new Pixel((short) x, (short) y);
            this.points.add(curvePoint);
        }

        this.points.sort(Comparator.comparingInt(point -> point.center.getX()));

        for (int i = 1; i < this.points.size() - 1; i++) {
            CurvePoint previous = this.points.get(i - 1);
            CurvePoint point = this.points.get(i);
            CurvePoint next = this.points.get(i + 1);

            float dx = (next.center.getX() - previous.center.getX()) / 2f;
            float dy = (next.center.getY() - previous.center.getY()) / 2f;
            point.angle = (float) Math.atan2(dy, dx);
        }
        this.points.remove(0);
        this.points.remove(this.points.size() - 1);
    }

    private float approximateCurveLength() {
        float distance1 = (float) Math.sqrt(Math.pow(this.farthestPoints[0].getX() - this.centroid.getX(), 2) + Math.pow(this.farthestPoints[0].getY() - this.centroid.getY(), 2));
        float distance2 = (float) Math.sqrt(Math.pow(this.farthestPoints[1].getX() - this.centroid.getX(), 2) + Math.pow(this.farthestPoints[1].getY() - this.centroid.getY(), 2));

        return distance1 + distance2;
    }

    private void setPointsOrigin(GlyphLayout layout) {
        layout.setText(font, this.label);

        for (int i = 0; i < this.points.size(); i++) {
            BitmapFont.Glyph glyph = layout.runs.first().glyphs.get(i);
            float charWidth = glyph.width * this.fontScale;
            float charHeight = glyph.height;

            short x = (short) (this.points.get(i).center.getX() - (charWidth / 2));
            short y = (short) (this.points.get(i).center.getY());

            this.points.get(i).origin = new Pixel(x, y);
        }
    }

    public void draw(SpriteBatch batch, ShaderProgram fontShader) {
        font.getData().setScale(this.fontScale);
        for (int i = 0; i < this.label.length(); i++) {
            fontShader.setUniformf("u_angle", this.points.get(i).angle);
            fontShader.setUniformf("u_center", this.points.get(i).center.getX(), this.points.get(i).center.getY());

            font.draw(batch, String.valueOf(this.label.charAt(i)), this.points.get(i).origin.getX(), this.points.get(i).origin.getY());
            batch.flush();
        }
        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
    }

}
