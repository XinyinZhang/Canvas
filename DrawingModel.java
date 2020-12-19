import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Stack;
import java.util.UUID;

public class DrawingModel extends Observable{
  private Map<UUID, ShapeModel> shapeModelMap = new HashMap<>();

  private Stack<History> undoStack;
  private Stack<History> redoStack;

  ShapeModel.ShapeType shapeType = ShapeModel.ShapeType.Rectangle;

  private UUID selectedShapeId;

  public ShapeModel.ShapeType getShape(){
    return shapeType;
  }

  public void setShape(ShapeModel.ShapeType shapeType){
    this.shapeType = shapeType;
  }

  public DrawingModel(){
    undoStack = new Stack<>();
    redoStack = new Stack<>();
  }

  public Collection<ShapeModel> getShapeModelList(){
    return Collections.unmodifiableCollection(shapeModelMap.values());
  }

  public void addShape(ShapeModel shapeModel){
    shapeModelMap.put(shapeModel.getUuid(), shapeModel);
    undoStack.push(History.createShapeHistory(shapeModel.getUuid(), shapeModel.getShape()));
    redoStack.empty();
    selectedShapeId = shapeModel.getUuid();

    setChanged();
    notifyObservers();
  }

  public void transformShape(AffineTransform transform){
    ShapeModel shapeModel = shapeModelMap.get(selectedShapeId);

    if(shapeModel != null){
      shapeModel.transform(transform);
      undoStack.push(History.createTransformHistory(shapeModel.getUuid(), transform));
      redoStack.empty();
      this.setChanged();
      this.notifyObservers();
    }
  }

  public void setSelectedShapeId(UUID selectedShapeId){
    this.selectedShapeId = selectedShapeId;
    this.setChanged();
    this.notifyObservers();
  }

  public UUID getSelectedShapeId(){
    return selectedShapeId;
  }

  public ShapeModel getSelectedShapeModel(){
    return shapeModelMap.get(selectedShapeId);
  }

  public void undo(){
    if(canUndo()){
      History history = undoStack.pop();
      ShapeModel shapeModel = shapeModelMap.get(history.getShapeModelUuid());

      if(shapeModel != null){
        if(history.getType() == History.TYPE.CREATE){
          shapeModelMap.remove(history.getShapeModelUuid());
          selectedShapeId = null;
        }
        else if(history.getType() == History.TYPE.TRANSFORM){
          try{
            shapeModel.transform(history.getTransform().createInverse());
          } catch(NoninvertibleTransformException e){
            e.printStackTrace();
          }
          selectedShapeId = shapeModel.getUuid();
        }
      }
      redoStack.push(history);
      setChanged();
      notifyObservers();
    }
  }

  public void redo(){
    if(canRedo()){
      History history = redoStack.pop();

      if(history.getType() == History.TYPE.CREATE){
        shapeModelMap.put(history.getShapeModelUuid(), new ShapeModel(history.getShape(), new AffineTransform(), history.getShapeModelUuid()));
        selectedShapeId = history.getShapeModelUuid();
      }
      else if(history.getType() == History.TYPE.TRANSFORM){
        ShapeModel shapeModel = shapeModelMap.get(history.getShapeModelUuid());

        if(shapeModel != null){
          shapeModel.transform(history.getTransform());
          selectedShapeId = shapeModel.getUuid();
        }
      }
      undoStack.push(history);
      setChanged();
      notifyObservers();
    }
  }

  public void duplicate(){
    ShapeModel shapeModel = shapeModelMap.get(selectedShapeId).duplicate();
    addShape(shapeModel);
  }

  public boolean canUndo(){
    return undoStack.size() > 0;
  }

  public boolean canRedo(){
    return redoStack.size() > 0;
  }

  public boolean canDuplicate(){
    return shapeModelMap.get(selectedShapeId) != null;
  }
}
