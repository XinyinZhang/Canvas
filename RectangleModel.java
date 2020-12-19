import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class RectangleModel extends ShapeModel {

    public RectangleModel(Point startPoint, Point endPoint) {
        super(startPoint, endPoint);

        Rectangle2D rect = new Rectangle2D.Double(startPoint.x,startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);

        this.shape = rect;
    }
}
