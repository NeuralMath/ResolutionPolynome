package resolution;

import java.util.ArrayList;
import java.text.DecimalFormat;

public class Term
{
    private int _position;                      //Position de la variable dans l'equation + 1. Devient obsolete une fois qu'on echange les termes.
    private String _character;                  //Le caractere de la variable.
    private double _coefficient;                //Le coefficient qui multiplie la variable.
    private double _exponent;                   //L'exposant de la variable. Un nombre a un exposant de 0.
    private String _operator;                   //L'operaeur devant le terme.
    private ArrayList<String> _openParenthesis; //Tableau des parentheses ouvrantes. L'operateur devant la parenthese ouvrante ou aucune parenthese ("") devant le terme. En ordre du plus proche au plus eloigne du terme.
    private ArrayList<String> _closeParenthesis;//Tableau des parentheses fermantes. Parenthese fermante ("0") ou aucune parenthese (""). En ordre du plus proche au plus eloigne du terme.
    
    DecimalFormat _df = new DecimalFormat("0.##");  //Format de l'affichage.
        
    Term()
    {        
        erase();
    }
    
    Term(int p, String c, double co, double e, String o, ArrayList<String> op, ArrayList<String> cp)
    {
        _position = p;
        _character = c;
        _coefficient = co;
        _exponent = e;
        _operator = o;
        _openParenthesis = op;
        _closeParenthesis = cp;
    }
    
    Term(Term t)
    {
        _position = t._position;
        _character = t._character;
        _coefficient = t._coefficient;
        _exponent = t._exponent;
        _operator = t._operator;
        _openParenthesis = new ArrayList(t._openParenthesis);
        _closeParenthesis = new ArrayList(t._closeParenthesis);
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
    
    ArrayList<String> getOpenParenthesis()
    {
        return _openParenthesis;
    }

    void setOpenParenthesis(ArrayList<String> op)
    {
        _openParenthesis = new ArrayList(op);
    }
    
    ArrayList<String> getCloseParenthesis()
    {
        return _closeParenthesis;
    }

    void setCloseParenthesis(ArrayList<String> cp)
    {
        _closeParenthesis = new ArrayList(cp);
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
        _coefficient = 0.0;
        _exponent = 0.0;
        _operator = "";
        _openParenthesis = null;
        _closeParenthesis = null;
    }

    String display()
    {
        String term = "";
        
        //S'il y a au moins une parenthese ouvrante.
        if (!_openParenthesis.isEmpty())
        {
            for (String open : _openParenthesis)
            {
                term += open + "(";
            }
        }
        
        term += _operator + _df.format(_coefficient) + "*" + _character + "^" + _df.format(_exponent);
        
        //S'il y a au moins une parenthese fermante.
        if (!_openParenthesis.isEmpty())
        {
            for (String close : _openParenthesis)
            {
                term += ")";
            }
        }
         
        return term;
    }
}
