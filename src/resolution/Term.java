package resolution;

public class Term
{
    int _position;		//Position de la variable dans l'equation + 1. Devient obsolete une fois qu'on echange les termes.
    String _character;          //Le caractere de la variable.
    String _operator;		//L'operaeur devant le terme.
    double _coefficient;	//Le coefficient qui multiplie la variable.
    double _exponent;		//L'exposant de la variable. Un nombre a un exposant de 0.
        
    Term()
    {        
        erase();
    }
    
    Term(Term t)
    {
        _position = t._position;
        _character = t._character;
        _operator = t._operator;
        _coefficient = t._coefficient;
        _exponent = t._exponent;
    }
    
    int getPosition()
    {
        return _position;
    }

    void setPosition(int p)
    {
        _position = p;
    }

    String getCharacter()
    {
        return _character;
    }

    void setCharacter(String c)
    {
        _character = c;
    }

    String getOperator()
    {
        return _operator;
    }

    void setOperator(String op)
    {
        _operator = op;
    }

    double getCoefficient()
    {
        return _coefficient;
    }

    void setCoefficient(double c)
    {
        _coefficient = c;
    }

    double getExponent()
    {
        return _exponent;
    }

    void setExponent(double e)
    {
        _exponent = e;
    }

    void erase()
    {
        _position = 0;
        _character = "";
        _operator = "";
        _coefficient = 0;
        _exponent = 0;
    }

    String display()
    {
        return _operator + Double.toString(_coefficient) + "*" + _character + "^" + Double.toString(_exponent);
    }
}
