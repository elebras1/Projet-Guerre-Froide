package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;

import java.util.List;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class MapLabel {
    private final String label;
    public int centroid;
    private int[] farthestPoints;
    private List<CurvePoint> points;
    private static final BitmapFont font = new BitmapFont(Gdx.files.internal("ui/fonts/kart_font.fnt"), true);
    private float fontScale;
    private static class CurvePoint {
        int center;
        int origin;
        float angle;
    }

    static {
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public MapLabel(String label, IntList borderPixels, IntList positionsProvinces) {
        this.label = label;
        IntList convexHull = this.getConvexHull(borderPixels);
        this.setCentroid(convexHull, positionsProvinces);
        this.findFarthestPoints(convexHull);
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, this.label);
        this.setFontScale(layout);
        this.calculateQuadraticBezierCurve();
        this.setPointsOrigin(layout);
    }

    private IntList getConvexHull(IntList borderPixels) {
        IntList approximateBorder = new IntList();
        int step = 5;

        int minPixelInt = this.getMinPixel(borderPixels);
        short minPixelX = (short) (minPixelInt >> 16);
        short minPixelY = (short) (minPixelInt & 0xFFFF);

        int maxPixelInt = this.getMaxPixel(borderPixels);
        short maxPixelX = (short) (maxPixelInt >> 16);
        short maxPixelY = (short) (maxPixelInt & 0xFFFF);

        for (int x = minPixelX; x <= maxPixelX; x += step) {
            int minYPoint = this.getMinYPointInRange(borderPixels, x, x + step);
            int maxYPoint = this.getMaxYPointInRange(borderPixels, x, x + step);

            if (minYPoint != -1) approximateBorder.add(minYPoint);
            if (maxYPoint != -1) approximateBorder.add(maxYPoint);
        }

        for (int y = minPixelY; y <= maxPixelY; y += step) {
            int minXPoint = this.getMinXPointInRange(borderPixels, y, y + step);
            int maxXPoint = this.getMaxXPointInRange(borderPixels, y, y + step);

            if (minXPoint != -1) approximateBorder.add(minXPoint);
            if (maxXPoint != -1) approximateBorder.add(maxXPoint);
        }

        return approximateBorder;
    }

    private int getMinYPointInRange(IntList pixels, int xStart, int xEnd) {
        int minYPoint = -1;
        int minYPointY = Integer.MAX_VALUE;
        for (int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if (pixelX >= xStart && pixelX < xEnd) {
                if (pixelY < minYPointY) {
                    minYPoint = pixelInt;
                    minYPointY = pixelY;
                }
            }
        }
        return minYPoint;
    }

    private int getMaxYPointInRange(IntList pixels, int xStart, int xEnd) {
        int maxYPoint = -1;
        int maxYPointY = -1;
        for (int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if (pixelX >= xStart && pixelX < xEnd) {
                if (pixelY > maxYPointY) {
                    maxYPoint = pixelInt;
                    maxYPointY = pixelY;
                }
            }
        }
        return maxYPoint;
    }

    private int getMinXPointInRange(IntList pixels, int yStart, int yEnd) {
        int minXPoint = -1;
        int minXPointX = Integer.MAX_VALUE;
        for (int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if (pixelY >= yStart && pixelY < yEnd) {
                if (pixelX < minXPointX) {
                    minXPoint = pixelInt;
                    minXPointX = pixelX;
                }
            }
        }
        return minXPoint;
    }

    private int getMaxXPointInRange(IntList pixels, int yStart, int yEnd) {
        int maxXPoint = -1;
        int maxXPointX = -1;
        for (int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if (pixelY >= yStart && pixelY < yEnd) {
                if (pixelX > maxXPointX) {
                    maxXPoint = pixelInt;
                    maxXPointX = pixelX;
                }
            }
        }
        return maxXPoint;
    }

    private int getMinPixel(IntList pixels) {
        short xMin = Short.MAX_VALUE;
        short yMin = Short.MAX_VALUE;
        for(int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if(pixelX < xMin) {
                xMin = pixelX;
            }
            if(pixelY < yMin) {
                yMin = pixelY;
            }
        }

        return (xMin << 16) | (yMin & 0xFFFF);
    }

    private int getMaxPixel(IntList pixels) {
        short xMax = 0;
        short yMax = 0;
        for(int i = 0; i < pixels.size(); i++) {
            int pixelInt = pixels.get(i);
            short pixelX = (short) (pixelInt >> 16);
            short pixelY = (short) (pixelInt & 0xFFFF);

            if(pixelX > xMax) {
                xMax = pixelX;
            }
            if(pixelY > yMax) {
                yMax = pixelY;
            }
        }

        return (xMax << 16) | (yMax & 0xFFFF);
    }

    private void setCentroid(IntList convexHull, IntList positionsProvinces) {
        int centerX = 0;
        int centerY = 0;

        for (int i = 0; i < convexHull.size(); i++) {
            int pixelInt = convexHull.get(i);
            centerX += (short) (pixelInt >> 16);
            centerY += (short) (pixelInt & 0xFFFF);
        }

        centerX /= convexHull.size();
        centerY /= convexHull.size();

        int closestPosition = -1;
        int minDistanceSquared = Integer.MAX_VALUE;

        for (int i = 0; i < positionsProvinces.size(); i++) {
            int positionInt = positionsProvinces.get(i);
            int px = (short) (positionInt >> 16);
            int py = (short) (positionInt & 0xFFFF);

            int distanceSquared = (px - centerX) * (px - centerX) + (py - centerY) * (py - centerY);

            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                closestPosition = positionInt;
            }
        }

        this.centroid = closestPosition;
    }

    private void findFarthestPoints(IntList pixels) {
        double maxDistance = 0;
        int farthestPoint1 = -1;
        int farthestPoint2 = -1;

        for (int i = 0; i < pixels.size(); i++) {
            for (int j = i + 1; j < pixels.size(); j++) {
                int pixelInt1 = pixels.get(i);
                short pixelX1 = (short) (pixelInt1 >> 16);
                short pixelY1 = (short) (pixelInt1 & 0xFFFF);

                int pixelInt2 = pixels.get(j);
                short pixelX2 = (short) (pixelInt2 >> 16);
                short pixelY2 = (short) (pixelInt2 & 0xFFFF);

                double distance = Math.sqrt(Math.pow(pixelX2 - pixelX1, 2) + Math.pow(pixelY2 - pixelY1, 2));

                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestPoint1 = pixelInt1;
                    farthestPoint2 = pixelInt2;
                }
            }
        }

        if((farthestPoint1 >> 16) < (farthestPoint2 >> 16)) {
            this.farthestPoints = new int[] {farthestPoint1, farthestPoint2};
        } else {
            this.farthestPoints = new int[] {farthestPoint2, farthestPoint1};
        }
    }

    private void setFontScale(GlyphLayout layout) {
        float textLength = layout.width;
        float curveLength = this.approximateCurveLength();
        this.fontScale = (curveLength / textLength) * 0.4f;
    }

    private void calculateQuadraticBezierCurve() {
        this.points = new ObjectList<>();

        int numberOfPoints = this.label.length() + 2;

        int farthestPoint1 = this.farthestPoints[0];
        short farthestPointX1 = (short) (farthestPoint1 >> 16);
        short farthestPointY1 = (short) (farthestPoint1 & 0xFFFF);

        int farthestPoint2 = this.farthestPoints[1];
        short farthestPointX2 = (short) (farthestPoint2 >> 16);
        short farthestPointY2 = (short) (farthestPoint2 & 0xFFFF);

        short centroidX = (short) (this.centroid >> 16);
        short centroidY = (short) (this.centroid & 0xFFFF);

        float compressionFactor = 0.7f;

        for (int i = 0; i < numberOfPoints; i++) {
            float progressionFactor = (float) i / (numberOfPoints - 1);

            float x = (1 - progressionFactor) * (1 - progressionFactor) * farthestPointX1 + 2 * (1 - progressionFactor) * progressionFactor * centroidX + progressionFactor * progressionFactor * farthestPointX2;
            float y = (1 - progressionFactor) * (1 - progressionFactor) * farthestPointY1 + 2 * (1 - progressionFactor) * progressionFactor * centroidY + progressionFactor * progressionFactor * farthestPointY2;

            x = centroidX + (x - centroidX) * compressionFactor;
            y = centroidY + (y - centroidY) * compressionFactor;

            CurvePoint curvePoint = new CurvePoint();
            curvePoint.center = ((int) x << 16) | ((int) y & 0xFFFF);
            this.points.add(curvePoint);
        }

        for (int i = 1; i < this.points.size() - 1; i++) {
            CurvePoint previous = this.points.get(i - 1);
            CurvePoint point = this.points.get(i);
            CurvePoint next = this.points.get(i + 1);

            short nextCenterX = (short) (next.center >> 16);
            short nextCenterY = (short) (next.center & 0xFFFF);

            short previousCenterX = (short) (previous.center >> 16);
            short previousCenterY = (short) (previous.center & 0xFFFF);

            float dx = (nextCenterX - previousCenterX) / 2f;
            float dy = (nextCenterY - previousCenterY) / 2f;
            point.angle = (float) Math.atan2(dy, dx) * (180f / (float) Math.PI);
        }

        this.points.remove(0);
        this.points.remove(this.points.size() - 1);
    }

    private float approximateCurveLength() {
        int farthestPoint1 = this.farthestPoints[0];
        short farthestPointX1 = (short) (farthestPoint1 >> 16);
        short farthestPointY1 = (short) (farthestPoint1 & 0xFFFF);

        int farthestPoint2 = this.farthestPoints[1];
        short farthestPointX2 = (short) (farthestPoint2 >> 16);
        short farthestPointY2 = (short) (farthestPoint2 & 0xFFFF);

        short centroidX = (short) (this.centroid >> 16);
        short centroidY = (short) (this.centroid & 0xFFFF);

        int dx1 = farthestPointX1 - centroidX;
        int dy1 = farthestPointY1 - centroidY;
        int dx2 = farthestPointX2 - centroidX;
        int dy2 = farthestPointY2 - centroidY;

        float distance1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
        float distance2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

        return distance1 + distance2;
    }

    private void setPointsOrigin(GlyphLayout layout) {
        float glyphHeight = layout.height * this.fontScale;
        for (int i = 0; i < this.points.size(); i++) {
            BitmapFont.Glyph glyph = layout.runs.first().glyphs.get(i);
            float glyphWidth = glyph.width * this.fontScale;

            short centerX = (short) (this.points.get(i).center >> 16);
            short centerY = (short) (this.points.get(i).center & 0xFFFF);

            short x = (short) (centerX - (glyphWidth / 2));
            short y = (short) (centerY - (glyphHeight / 2));

            this.points.get(i).origin = (x << 16) | (y & 0xFFFF);
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion textureRegionFont = font.getRegion();
        CurvePoint curvePoint;
        BitmapFont.Glyph glyph;
        for (int i = 0; i < this.label.length(); i++) {
            curvePoint = this.points.get(i);
            glyph = font.getData().getGlyph(this.label.charAt(i));
            textureRegionFont.setRegion(glyph.u, glyph.v, glyph.u2, glyph.v2);
            float glyphWidth = glyph.width * this.fontScale;
            float glyphHeight = glyph.height * this.fontScale;

            short originX = (short) (curvePoint.origin >> 16);
            short originY = (short) (curvePoint.origin & 0xFFFF);

            batch.draw(
                textureRegionFont,
                originX - WORLD_WIDTH,
                originY,
                glyphWidth / 2,
                glyphHeight / 2,
                glyphWidth,
                glyphHeight,
                1,
                1,
                curvePoint.angle
            );

            batch.draw(
                textureRegionFont,
                originX,
                originY,
                glyphWidth / 2,
                glyphHeight / 2,
                glyphWidth,
                glyphHeight,
                1,
                1,
                curvePoint.angle
            );

            batch.draw(
                textureRegionFont,
                originX + WORLD_WIDTH,
                originY,
                glyphWidth / 2,
                glyphHeight / 2,
                glyphWidth,
                glyphHeight,
                1,
                1,
                curvePoint.angle
            );
        }

        Gdx.gl.glActiveTexture(GL32.GL_TEXTURE0);
    }
}
