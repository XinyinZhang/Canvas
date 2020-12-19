import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class CanvasView extends JPanel implements Observer{
  DrawingModel model;
  Point2D lastMouse;
  Point2D startMouse;
  DRAG_TYPE dragType;

  enum DRAG_TYPE{
    NewShape,
    Translate,
    Rotate,
    Scale
  }

  public CanvasView(DrawingModel model){
    super();
    this.model = model;

    MouseAdapter mouseListener = new MouseAdapter(){
      @Override
      public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        lastMouse = e.getPoint();
        startMouse = e.getPoint();

        if(model.getSelectedShapeId() != null){
          ShapeModel shapeModel = model.getSelectedShapeModel();

          if(shapeModel.hitTestRotateHandle(startMouse)){
            dragType = DRAG_TYPE.Rotate;
          }
          else if(shapeModel.hitTestScaleHandle(startMouse)){
            dragType = DRAG_TYPE.Scale;
          }
          else if(shapeModel.hitTest(startMouse)){
            dragType = DRAG_TYPE.Translate;
          }
          else{
            model.setSelectedShapeId(null);
            dragType = DRAG_TYPE.NewShape;
          }
        }
        else{
          dragType = DRAG_TYPE.NewShape;
        }
      }

      @Override
      public void mouseDragged(MouseEvent e){
        super.mouseDragged(e);
        lastMouse = e.getPoint();
        repaint();
      }

      @Override
      public void mouseReleased(MouseEvent e){
        super.mouseReleased(e);

        if(startMouse.equals(lastMouse)){
          for(ShapeModel shapeModel : model.getShapeModelList()){
            if(shapeModel.hitTest(lastMouse)){
              model.setSelectedShapeId(shapeModel.getUuid());
              break;
            }
          }
        }
        else if(dragType == DRAG_TYPE.NewShape){
          ShapeModel shape = new ShapeModel.ShapeFactory().getShape(model.getShape(), (Point) startMouse, (Point) lastMouse);
          model.addShape(shape);
        }
        else if(dragType == DRAG_TYPE.Translate){
          model.transformShape(getTransform());
        }
        else if(dragType == DRAG_TYPE.Rotate){
          model.transformShape(getTransform());
        }
        else if(dragType == DRAG_TYPE.Scale){
          model.transformShape(getTransform());
        }

        dragType = null;
        startMouse = null;
        lastMouse = null;
        repaint();
      }
    };

    this.addMouseListener(mouseListener);
    this.addMouseMotionListener(mouseListener);

    model.addObserver(this);
  }

  @Override
  public void update(Observable o, Object arg){
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    setBackground(Color.WHITE);

    drawAllShapes(g2);
    drawCurrentShape(g2);
  }

  private void drawAllShapes(Graphics2D g2){

    g2.setColor(new Color(66, 66, 66));
    g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    for(ShapeModel shape : model.getShapeModelList()){
      if((model.getSelectedShapeModel() == null) || !model.getSelectedShapeModel().getUuid().equals(shape.getUuid())){
        g2.draw(shape.getShape());
      }
    }
  }

  private void drawCurrentShape(Graphics2D g2){
    if(dragType == DRAG_TYPE.NewShape && startMouse != null){
      g2.setColor(new Color(66, 66, 66));
      g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.draw(new ShapeModel.ShapeFactory().getShape(model.getShape(), (Point) startMouse, (Point) lastMouse).getShape());
    }
    else if(model.getSelectedShapeModel() != null){
      AffineTransform transform = getTransform();
      g2.setColor(new Color(66, 66, 66));
      g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.draw(transform.createTransformedShape(model.getSelectedShapeModel().getShape()));

      g2.setColor(new Color(66, 66, 200));
      g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.fill(model.getSelectedShapeModel().getScaleHandle(transform));
      g2.fill(model.getSelectedShapeModel().getRotateHandle(transform));
    }
  }

  private AffineTransform getTransform(){
    AffineTransform transform = new AffineTransform();

    if(model.getSelectedShapeModel() != null){
      Rectangle bounds = model.getSelectedShapeModel().getShape().getBounds();

      if(dragType == DRAG_TYPE.Translate){
        transform.translate(lastMouse.getX() - startMouse.getX(), lastMouse.getY() - startMouse.getY());
      }
      else if(dragType == DRAG_TYPE.Scale){
        Point2D startingPoint = model.getSelectedShapeModel().getShapeStartPoint();
        double scaleX = (lastMouse.getX() - bounds.x) / (startMouse.getX() - bounds.x);
        double scaleY = (lastMouse.getY() - bounds.y) / (startMouse.getY() - bounds.y);
        double thingyX = 0;
        double thingyY = 0;

        if(startingPoint.getX() > startMouse.getX()){
          thingyX = bounds.width;
          scaleX = ((bounds.x + bounds.width) - lastMouse.getX()) / ((bounds.x + bounds.width) - startMouse.getX());
        }
        if(startingPoint.getY() > startMouse.getY()){
          thingyY = bounds.height;
          scaleY = ((bounds.y + bounds.height) - lastMouse.getY()) / ((bounds.y + bounds.height) - startMouse.getY());
        }
        transform.translate(bounds.x + thingyX, bounds.y + thingyY);
        transform.scale(scaleX, scaleY);
        transform.translate(-(bounds.x + thingyX), -(bounds.y + thingyY));
      }
      else if(dragType == DRAG_TYPE.Rotate){
        Point2D centerPoint = new Point2D.Double(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
        Point2D originalDirectionVector = new Point2D.Double(startMouse.getX() - centerPoint.getX(), startMouse.getY() - centerPoint.getY());
        Point2D newDirectionVector = new Point2D.Double(lastMouse.getX() - centerPoint.getX(), lastMouse.getY() - centerPoint.getY());
        double angle = Math.atan2(newDirectionVector.getY(), newDirectionVector.getX()) - Math.atan2(originalDirectionVector.getY(), originalDirectionVector.getX());
        transform.rotate(angle, centerPoint.getX(), centerPoint.getY());
      }
    }
    return transform;
  }
}
