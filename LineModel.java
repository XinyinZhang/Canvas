import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class LineModel extends ShapeModel {

    Point a;
    Point b;
    public LineModel(Point startPoint, Point endPoint) {
        super(startPoint, endPoint);
        this.a = startPoint;
        this.b = endPoint;

        Path2D path = new Path2D.Double();
        path.moveTo(startPoint.x, startPoint.y);
        path.lineTo(endPoint.x, endPoint.y);
        this.shape = path;
    }

    @Override
    public boolean hitTest(Point2D p) {
        return pointToLineDistance(a,b,(Point) p) < 10;
    }

    public double pointToLineDistance(Point A, Point B, Point P) {
        double normalLength = Math.sqrt((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
        return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
    }
}