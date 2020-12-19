import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JToolBar;

public class ToolbarView extends JToolBar implements Observer{
  private JButton undo = new JButton("Undo");
  private JButton redo = new JButton("Redo");
  private JButton duplicate = new JButton("Duplicate");

  private DrawingModel model;

  ToolbarView(DrawingModel model){
    super();
    this.model = model;
    model.addObserver(this);

    setFloatable(false);

    undo.addActionListener(e -> model.undo());

    redo.addActionListener(e -> model.redo());

    duplicate.addActionListener(e -> model.duplicate());

    add(undo);
    add(redo);
    add(duplicate);

    ActionListener drawingActionListener = e -> model.setShape(ShapeModel.ShapeType.valueOf(((JButton) e.getSource()).getText()));

    for(ShapeModel.ShapeType mode : ShapeModel.ShapeType.values()){
      JButton button = new JButton(mode.toString());
      button.addActionListener(drawingActionListener);
      add(button);
    }

    this.update(null, null);
  }

  @Override
  public void update(Observable o, Object arg){
    undo.setEnabled(model.canUndo());
    redo.setEnabled(model.canRedo());
    duplicate.setEnabled(model.canDuplicate());
  }
}
