package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.g2d.*;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ShortList;

import java.util.List;

public class MapLabel {
    private final BitmapFont font;
    private static class CurvePoint {
        int center;
        int origin;
        float angle;
    }

    public MapLabel(BitmapFont font) {
        this.font = font;
    }

    public void generateData(String label, IntList borderPixels, IntList positionsProvinces, FloatList vertices, ShortList indices) {
        IntList convexHull = this.getConvexHull(borderPixels);
        int centroid = this.getCentroid(convexHull, positionsProvinces);
        int[] farthestPoints = this.getFarthestPoints(convexHull);
        GlyphLayout layout = new GlyphLayout();
        layout.setText(this.font, label);
        float fontScale = this.getFontScale(layout, farthestPoints, centroid);
        List<CurvePoint> points = this.calculateQuadraticBezierCurve(label, farthestPoints, centroid);
        this.setPointsOrigin(layout, fontScale, points);
        this.addMeshData(vertices, indices, label, points, fontScale);
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
            int minYIndex = this.getMinYIndexInRange(borderPixels, x, x + step);
            int maxYIndex = this.getMaxYIndexInRange(borderPixels, x, x + step);

            if (minYIndex != -1) {
                approximateBorder.add(borderPixels.get(minYIndex));
                approximateBorder.add(borderPixels.get(minYIndex + 1));
            }
            if (maxYIndex != -1) {
                approximateBorder.add(borderPixels.get(maxYIndex));
                approximateBorder.add(borderPixels.get(maxYIndex + 1));
            }
        }

        for (int y = minPixelY; y <= maxPixelY; y += step) {
            int minXIndex = this.getMinXIndexInRange(borderPixels, y, y + step);
            int maxXIndex = this.getMaxXIndexInRange(borderPixels, y, y + step);

            if (minXIndex != -1) {
                approximateBorder.add(borderPixels.get(minXIndex));
                approximateBorder.add(borderPixels.get(minXIndex + 1));
            }
            if (maxXIndex != -1) {
                approximateBorder.add(borderPixels.get(maxXIndex));
                approximateBorder.add(borderPixels.get(maxXIndex + 1));
            }
        }

        return approximateBorder;
    }

    private int getMinYIndexInRange(IntList pixels, int xStart, int xEnd) {
        int index = -1;
        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

            if (pixelX >= xStart && pixelX < xEnd) {
                if (pixelY < minY) {
                    minY = pixelY;
                    index = i;
                }
            }
        }
        return index;
    }

    private int getMaxYIndexInRange(IntList pixels, int xStart, int xEnd) {
        int index = -1;
        int maxY = -1;
        for (int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

            if (pixelX >= xStart && pixelX < xEnd) {
                if (pixelY > maxY) {
                    maxY = pixelY;
                    index = i;
                }
            }
        }
        return index;
    }

    private int getMinXIndexInRange(IntList pixels, int yStart, int yEnd) {
        int index = -1;
        int minX = Integer.MAX_VALUE;
        for (int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

            if (pixelY >= yStart && pixelY < yEnd) {
                if (pixelX < minX) {
                    minX = pixelX;
                    index = i;
                }
            }
        }
        return index;
    }

    private int getMaxXIndexInRange(IntList pixels, int yStart, int yEnd) {
        int index = -1;
        int maxX = -1;
        for (int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

            if (pixelY >= yStart && pixelY < yEnd) {
                if (pixelX > maxX) {
                    maxX = pixelX;
                    index = i;
                }
            }
        }
        return index;
    }

    private int getMinPixel(IntList pixels) {
        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        for(int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

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
        int xMax = 0;
        int yMax = 0;
        for(int i = 0; i < pixels.size(); i += 2) {
            int pixelX = pixels.get(i);
            int pixelY = pixels.get(i + 1);

            if(pixelX > xMax) {
                xMax = pixelX;
            }
            if(pixelY > yMax) {
                yMax = pixelY;
            }
        }

        return (xMax << 16) | (yMax & 0xFFFF);
    }

    private int getCentroid(IntList convexHull, IntList positionsProvinces) {
        long centerX = 0;
        long centerY = 0;

        for (int i = 0; i < convexHull.size(); i += 2) {
            centerX += convexHull.get(i);
            centerY += convexHull.get(i + 1);
        }

        if (convexHull.size() > 0) {
            centerX /= (convexHull.size() / 2);
            centerY /= (convexHull.size() / 2);
        }

        int closestPosition = -1;
        long minDistanceSquared = Long.MAX_VALUE;

        for (int i = 0; i < positionsProvinces.size(); i += 2) {
            int px = positionsProvinces.get(i);
            int py = positionsProvinces.get(i + 1);

            long distanceSquared = (px - centerX) * (px - centerX) + (py - centerY) * (py - centerY);

            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                closestPosition = (px << 16) | (py & 0xFFFF);
            }
        }

        return closestPosition;
    }

    private int[] getFarthestPoints(IntList pixels) {
        double maxDistance = 0;
        int farthestPoint1 = -1;
        int farthestPoint2 = -1;

        for (int i = 0; i < pixels.size(); i += 2) {
            int pixelX1 = pixels.get(i);
            int pixelY1 = pixels.get(i + 1);

            for (int j = i + 2; j < pixels.size(); j += 2) {
                int pixelX2 = pixels.get(j);
                int pixelY2 = pixels.get(j + 1);

                double distance = Math.sqrt(Math.pow(pixelX2 - pixelX1, 2) + Math.pow(pixelY2 - pixelY1, 2));

                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestPoint1 = (pixelX1 << 16) | (pixelY1 & 0xFFFF);
                    farthestPoint2 = (pixelX2 << 16) | (pixelY2 & 0xFFFF);
                }
            }
        }

        if((farthestPoint1 >> 16) < (farthestPoint2 >> 16)) {
            return new int[] {farthestPoint1, farthestPoint2};
        } else {
            return new int[] {farthestPoint2, farthestPoint1};
        }
    }

    private float getFontScale(GlyphLayout layout, int[] farthestPoints, int centroid) {
        float textLength = layout.width;
        float curveLength = this.approximateCurveLength(farthestPoints, centroid);
        return (curveLength / textLength) * 0.4f;
    }

    private List<CurvePoint> calculateQuadraticBezierCurve(String label, int[] farthestPoints, int centroid) {
        List<CurvePoint> points = new ObjectList<>();

        int numberOfPoints = label.length() + 2;

        int farthestPoint1 = farthestPoints[0];
        short farthestPointX1 = (short) (farthestPoint1 >> 16);
        short farthestPointY1 = (short) (farthestPoint1 & 0xFFFF);

        int farthestPoint2 = farthestPoints[1];
        short farthestPointX2 = (short) (farthestPoint2 >> 16);
        short farthestPointY2 = (short) (farthestPoint2 & 0xFFFF);

        short centroidX = (short) (centroid >> 16);
        short centroidY = (short) (centroid & 0xFFFF);

        float compressionFactor = 0.7f;

        for (int i = 0; i < numberOfPoints; i++) {
            float progressionFactor = (float) i / (numberOfPoints - 1);

            float x = (1 - progressionFactor) * (1 - progressionFactor) * farthestPointX1 + 2 * (1 - progressionFactor) * progressionFactor * centroidX + progressionFactor * progressionFactor * farthestPointX2;
            float y = (1 - progressionFactor) * (1 - progressionFactor) * farthestPointY1 + 2 * (1 - progressionFactor) * progressionFactor * centroidY + progressionFactor * progressionFactor * farthestPointY2;

            x = centroidX + (x - centroidX) * compressionFactor;
            y = centroidY + (y - centroidY) * compressionFactor;

            CurvePoint curvePoint = new CurvePoint();
            curvePoint.center = ((int) x << 16) | ((int) y & 0xFFFF);
            points.add(curvePoint);
        }

        for (int i = 1; i < points.size() - 1; i++) {
            CurvePoint previous = points.get(i - 1);
            CurvePoint point = points.get(i);
            CurvePoint next = points.get(i + 1);

            short nextCenterX = (short) (next.center >> 16);
            short nextCenterY = (short) (next.center & 0xFFFF);

            short previousCenterX = (short) (previous.center >> 16);
            short previousCenterY = (short) (previous.center & 0xFFFF);

            float dx = (nextCenterX - previousCenterX) / 2f;
            float dy = (nextCenterY - previousCenterY) / 2f;
            point.angle = (float) Math.atan2(dy, dx) * (180f / (float) Math.PI);
        }

        points.remove(0);
        points.remove(points.size() - 1);
        return points;
    }

    private float approximateCurveLength(int[] farthestPoints, int centroid) {
        int farthestPoint1 = farthestPoints[0];
        short farthestPointX1 = (short) (farthestPoint1 >> 16);
        short farthestPointY1 = (short) (farthestPoint1 & 0xFFFF);

        int farthestPoint2 = farthestPoints[1];
        short farthestPointX2 = (short) (farthestPoint2 >> 16);
        short farthestPointY2 = (short) (farthestPoint2 & 0xFFFF);

        short centroidX = (short) (centroid >> 16);
        short centroidY = (short) (centroid & 0xFFFF);

        int dx1 = farthestPointX1 - centroidX;
        int dy1 = farthestPointY1 - centroidY;
        int dx2 = farthestPointX2 - centroidX;
        int dy2 = farthestPointY2 - centroidY;

        float distance1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
        float distance2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

        return distance1 + distance2;
    }

    private void setPointsOrigin(GlyphLayout layout, float fontScale, List<CurvePoint> points) {
        float glyphHeight = layout.height * fontScale;
        int processCount = Math.min(points.size(), layout.runs.first().glyphs.size);

        for (int i = 0; i < processCount; i++) {
            BitmapFont.Glyph glyph = layout.runs.first().glyphs.get(i);
            float glyphWidth = glyph.width * fontScale;

            short centerX = (short) (points.get(i).center >> 16);
            short centerY = (short) (points.get(i).center & 0xFFFF);

            short x = (short) (centerX - (glyphWidth / 2));
            short y = (short) (centerY - (glyphHeight / 2));

            points.get(i).origin = (x << 16) | (y & 0xFFFF);
        }
    }

    private void addMeshData(FloatList vertices, ShortList indices, String label, List<CurvePoint> points, float fontScale) {
        for (int i = 0; i < label.length(); i++) {
            CurvePoint curvePoint = points.get(i);
            BitmapFont.Glyph glyph = this.font.getData().getGlyph(label.charAt(i));
            if (glyph == null) {
                continue;
            }

            float u = glyph.u;
            float v = glyph.v;
            float u2 = glyph.u2;
            float v2 = glyph.v2;

            float glyphWidth = glyph.width * fontScale;
            float glyphHeight = glyph.height * fontScale;

            float pivotX = glyphWidth / 2f;
            float pivotY = glyphHeight / 2f;

            float posX = (short) (curvePoint.origin >> 16) + pivotX;
            float posY = (short) (curvePoint.origin & 0xFFFF) + pivotY;

            float scaleX = 1f;
            float scaleY = -1f;

            float lx1 = -pivotX * scaleX;
            float ly1 = -pivotY * scaleY;
            float lx2 = (glyphWidth - pivotX) * scaleX;
            float ly2 = -pivotY * scaleY;
            float lx3 = (glyphWidth - pivotX) * scaleX;
            float ly3 = (glyphHeight - pivotY) * scaleY;
            float lx4 = -pivotX * scaleX;
            float ly4 = (glyphHeight - pivotY) * scaleY;

            float angleRad = (float) Math.toRadians(curvePoint.angle);
            float cos = (float) Math.cos(angleRad);
            float sin = (float) Math.sin(angleRad);

            float x1 = posX + (lx1 * cos - ly1 * sin);
            float y1 = posY + (lx1 * sin + ly1 * cos);

            float x2 = posX + (lx2 * cos - ly2 * sin);
            float y2 = posY + (lx2 * sin + ly2 * cos);

            float x3 = posX + (lx3 * cos - ly3 * sin);
            float y3 = posY + (lx3 * sin + ly3 * cos);

            float x4 = posX + (lx4 * cos - ly4 * sin);
            float y4 = posY + (lx4 * sin + ly4 * cos);

            int startIndex = vertices.size() / 4;

            vertices.add(x1, y1, u, v2);
            vertices.add(x2, y2, u2, v2);
            vertices.add(x3, y3, u2, v);
            vertices.add(x4, y4, u, v);

            indices.add((short) startIndex);
            indices.add((short) (startIndex + 1));
            indices.add((short) (startIndex + 2));

            indices.add((short) startIndex);
            indices.add((short) (startIndex + 2));
            indices.add((short) (startIndex + 3));
        }
    }
}
