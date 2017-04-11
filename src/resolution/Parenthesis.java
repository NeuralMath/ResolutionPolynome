package resolution;

public class Parenthesis
{
    int _position;      //La position de la parenthese dans l'equation.
    boolean _open;       //Ouverte (vrai) ou fermante (false).
    
    Parenthesis()
    {
        _position = 0;
        _open = true;
    }
    
    Parenthesis(int p, boolean o)
    {
        _position = p;
        _open = o;
    }
    
    int getPosition()
    {
        return _position;
    }
    
    void setPosition(int p)
    {
        _position = p;
    }
    
    boolean isOpen()
    {
        return _open;
    }
    
    void setOpen(boolean o)
    {
        _open = o;
    }
}
