package resolution;

public class Parenthesis
{
    int _position;      //La position de la parenthese.
    String _operator;   //L'operateur devant la parenthese.
    
    Parenthesis()
    {
        _position = 0;
        _operator = "";
    }
    
    Parenthesis(int p, String o)
    {
        _position = p;
        _operator = o;
    }
    
    int getPosition()
    {
        return _position;
    }
    
    String getOperator()
    {
        return _operator;
    }
}