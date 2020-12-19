import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.UUID;

public class History{
  public enum TYPE{
    CREATE,
    TRANSFORM
  }

  private UUID shapeModelUuid;
  private TYPE type;
  private Shape shape;
  private AffineTransform transform;

  public static History createShapeHistory(UUID uuid, Shape shape){
    History history = new History();
    history.type = TYPE.CREATE;
    history.shape = shape;
    history.shapeModelUuid = uuid;
    return history;
  }

  public static History createTransformHistory(UUID uuid, AffineTransform transform){
    History history = new History();
    history.type = TYPE.TRANSFORM;
    history.transform = transform;
    history.shapeModelUuid = uuid;
    return history;
  }

  public UUID getShapeModelUuid(){
    return shapeModelUuid;
  }

  public TYPE getType(){
    return type;
  }

  public Shape getShape(){
    return shape;
  }

  public AffineTransform getTransform(){
    return transform;
  }
}
