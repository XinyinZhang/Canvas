import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.util.UUID;

public class ShapeModel{
  protected Shape shape;
  private UUID uuid;
  private AffineTransform transform;

  public ShapeModel(Point startPoint, Point endPoint){
    transform = new AffineTransform();
    uuid = UUID.randomUUID();
  }

  public ShapeModel(Shape shape, AffineTransform transform){
    this(shape, transform, UUID.randomUUID());
  }

  public ShapeModel(Shape shape, AffineTransform transform, UUID uuid){
    this.shape = shape;
    this.uuid = uuid;
    this.transform = transform;
  }

  public Shape getRotateHandle(AffineTransform tempTransform){
    Rectangle bounds = shape.getBounds();
    Point2D point = new Point2D.Double(bounds.x + (bounds.width / 2), bounds.y);
    Point2D centerPoint = new Point2D.Double(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);

    AffineTransform newTransform = new AffineTransform(transform);

    if(tempTransform != null){
      newTransform.preConcatenate(tempTransform);
    }
    newTransform.transform(point, point);
    newTransform.transform(centerPoint, centerPoint);

    Point2D direction = new Point.Double(point.getX() - centerPoint.getX(), point.getY() - centerPoint.getY());
    double magnitude = Math.sqrt((direction.getX() * direction.getX()) + (direction.getY() * direction.getY()));
    direction.setLocation(direction.getX() / magnitude, direction.getY() / magnitude);
    AffineTransform offsetTransform = new AffineTransform();
    offsetTransform.translate(direction.getX() * 10, direction.getY() * 10);

    offsetTransform.transform(point, point);
    return new Ellipse2D.Double(point.getX() - 5, point.getY() - 5, 10, 10);
  }

  public Shape getScaleHandle(AffineTransform tempTransform){
    Rectangle bounds = shape.getBounds();
    Point2D point = new Point2D.Double(bounds.x + bounds.width, bounds.y + bounds.height);
    transform.transform(point, point);

    if(tempTransform != null){
      tempTransform.transform(point, point);
    }
    return new Rectangle2D.Double(point.getX() - 5, point.getY() - 5, 10, 10);
  }

  public Shape getShape(){
    return transform.createTransformedShape(shape);
  }

  public Point2D getShapeStartPoint(){
    return transform.transform(new Point2D.Double(shape.getBounds().x, shape.getBounds().y), null);
  }

  // You will need to change the hittest to account for transformations.
  public boolean hitTest(Point2D p){
    return getShape().contains(p);
  }

  public boolean hitTestRotateHandle(Point2D p){
    return getRotateHandle(null).contains(p);
  }

  public boolean hitTestScaleHandle(Point2D p){
    return getScaleHandle(null).contains(p);
  }

  /**
   * Given a ShapeType and the start and end point of the shape, ShapeFactory constructs a new ShapeModel
   * using the class reference in the ShapeType enum and returns it.
   */
  public static class ShapeFactory{
    public ShapeModel getShape(ShapeType shapeType, Point startPoint, Point endPoint){
      try{
        Class<? extends ShapeModel> clazz = shapeType.shape;
        Constructor<? extends ShapeModel> constructor = clazz.getConstructor(Point.class, Point.class);

        ShapeModel shapeModel = constructor.newInstance(startPoint, endPoint);
        return shapeModel;
      } catch(Exception e){
        e.printStackTrace();
        return null;
      }
    }
  }

  public enum ShapeType{
    Ellipse(EllipseModel.class),
    Rectangle(RectangleModel.class),
    Line(LineModel.class);

    public final Class<? extends ShapeModel> shape;

    ShapeType(Class<? extends ShapeModel> shape){
      this.shape = shape;
    }
  }

  public UUID getUuid(){
    return uuid;
  }

  public void transform(AffineTransform transform){
    this.transform.preConcatenate(transform);
  }

  public ShapeModel duplicate(){
    AffineTransform newTransform = new AffineTransform();
    newTransform.concatenate(transform);
    newTransform.translate(10, 10);
    return new ShapeModel(shape, newTransform);
  }
}
