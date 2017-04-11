package resolution;

import java.util.ArrayList;
import java.text.DecimalFormat;

class Resolution
{
    String _equation;                           //L'equation a resoudre.
    ArrayList<String> _variables;               //Les variables dans l'equation.
    ArrayList<Parenthesis> _leftParenthesis;    //Les parentheses a gauche du egal.
    ArrayList<Parenthesis> _rightParenthesis;    //Les parentheses a droite du egal.
    ArrayList<Term> _leftTerms;                 //Les termes de l'equation a gauche du egal.
    ArrayList<Term> _rightTerms;                //Les termes de l'equation a droite du egal.
    int _equationLegthDisplay;                  //La grandeur de l'equation dans l'affichage. Sert uniquement a l'affichage.
    
    DecimalFormat _df = new DecimalFormat("0.##");  //Format de l'affichage.
    
    Resolution(String equation, ArrayList<String> var)
    {
	_equation = equation;
        
        _variables = new ArrayList();
	_variables = var;
        
        _leftTerms = new ArrayList();
        _rightTerms = new ArrayList();
        
	_equationLegthDisplay = 0;

	System.out.println("Equation:\t" + _equation + "\n");
    }

    int findCharPositionInString(String c, String s, int begin)
    {
        for (int i = begin; i < s.length(); i++)
        {
            if (s.charAt(i) == c.charAt(0))
            {
                return i + 1;	//La position juste apres le caractere
            }
        }

        return 0;
    }

    void fillTerms()
    {        
        Term temp = new Term();

        for (int i = 0; i < _variables.size(); i++)
        {
            int posNextVar = findCharPositionInString(_variables.get(i), _equation, 0), posEqual = findCharPositionInString("=", _equation, 0);

            while (posNextVar != 0)
            {
                for (int j = 0; j < _variables.size(); j++)
                {
                    if (_equation.charAt(posNextVar - 1) == _variables.get(i).charAt(0))
                    {
                        temp.setCharacter(_variables.get(i));
                    }
                }

                temp.setPosition(posNextVar);
                temp.setCoefficient(findTermCoefficient(posNextVar, _equation));
                temp.setExponent(findTermExponent(posNextVar, _equation));
                temp.setOperator(findTermOperator(posNextVar, _equation));

                if (posNextVar < posEqual)	//La variable est a gauche du egal.
                {                    
                    _leftTerms.add(new Term(temp));
                }
                else                            //La variable est a droite du egal
                {
                    _rightTerms.add(new Term(temp));
                }
                
                temp.erase();

                posNextVar = findCharPositionInString(_variables.get(i), _equation, posNextVar);
            }
        }
        
        fixSignOperators();
        
        updateEquation("Equation recue");
    }

    double findTermCoefficient(int pos, String s)
    {
        String temp = "";

        for (int i = pos - 3; i >= 0; i--)  //Recule de 2 positions avant la variable puis ramasse tous les chiffres.
        {
            if (Character.isDigit(s.charAt(i)))
            {
                temp = s.charAt(i) + temp;
            }
            else
            {
                break;
            }
        }

        return Double.parseDouble(temp);
    }
    
    //PPPPRRROOOOOBBB
    double findTermExponent(int pos, String s)
    {
        int i = pos + 1;
        String temp = "";

        //Si l'exposant est negatif.
        if (s.charAt(pos + 1) == '-')
        {
            temp += "-";
            i++;            //Commence au prochain caractere.
        }
        
        for (; i < s.length(); i++)  //Avance de 2 positions apres la variable puis ramasse tous les chiffres.
        {            
            if (Character.isDigit(s.charAt(i)))
            {
                temp = temp + s.charAt(i);
            }
            else
            {
                break;
            }
        }

        return Double.parseDouble(temp);
    }

    String findTermOperator(int pos, String s)
    {
        String temp = "";

        for (int i = pos - 3; i >= 0; i--)  //Avance de 2 positions avant la variable puis ramasse tous les chiffres.
        {
            if (!Character.isDigit(s.charAt(i)))
            {
                temp += s.charAt(i);
                break;
            }
        }

        return temp;
    }

    void fixSignOperators()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            if (_leftTerms.get(i).getOperator().equals("-"))
            {
                _leftTerms.get(i).setOperator("+");                                           //Affecte un operateur avec un "+" au terme.
                _leftTerms.get(i).setCoefficient(-1 * _leftTerms.get(i).getCoefficient());    //Inverse le signe du coefficient du terme correspondant.
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            if (_rightTerms.get(i).getOperator().equals("-"))
            {
                _rightTerms.get(i).setOperator("+");                                            //Affecte un operateur avec un "+" au terme.
                _rightTerms.get(i).setCoefficient(-1 * _rightTerms.get(i).getCoefficient());    //Inverse le signe du coefficient du terme correspondant.
            }
        }
    }

    void sortTermsViaGroups()
    {        
        ArrayList<Term> termsTemp = new ArrayList();
        ArrayList<ArrayList<Term>> groupsTerms = new ArrayList();

        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //Si le groupe est vide ou que l'operateur du terme n'est pas un "*" ou un "/".
            if (termsTemp.isEmpty() || !_leftTerms.get(i).getOperator().equals("+"))
            {
                termsTemp.add(new Term(_leftTerms.get(i)));         //Copie le dans le groupe.
            }
            else
            {
                groupsTerms.add(new ArrayList(termsTemp));          //Le groupe est complet. Met le dans le ArrayList de groupes.
                
                termsTemp.clear();                                  //Puis effaces-le.
                i--;                                                //Recommence pour le meme terme, donc decremente i.
            }
        }
        groupsTerms.add(new ArrayList(termsTemp));                  //Le dernier groupe est complet. Met le dans le ArrayList de groupes.

        groupsTerms = sortInGroups(groupsTerms);

        //Copie groupsTerms dans _leftTerms.
        _leftTerms.clear();
        for (int i = 0; i < groupsTerms.size(); i++)
        {
            for (int j = 0; j < groupsTerms.get(i).size(); j++)
            {
                _leftTerms.add(new Term(groupsTerms.get(i).get(j)));
            }
        }

        termsTemp.clear();
        groupsTerms.clear();


        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            //Si le groupe est vide ou que l'operateur du terme n'est pas un "*" ou un "/".
            if (termsTemp.isEmpty() || !_rightTerms.get(i).getOperator().equals("+"))
            {
                termsTemp.add(new Term(_rightTerms.get(i)));        //Copie le dans le groupe.
            }
            else
            {
                groupsTerms.add(new ArrayList(termsTemp));          //Le groupe est complet. Met le dans le ArrayList de groupes.
                
                termsTemp.clear();                                  //Puis effaces-le.
                i--;                                                //Recommence pour le meme terme, donc decremente i.
            }
        }
        groupsTerms.add(new ArrayList(termsTemp));                  //Le dernier groupe est complet. Met le dans le ArrayList de groupes.

        groupsTerms = sortInGroups(groupsTerms);

        //Copie groupsTerms dans _rightTerms.
        _rightTerms.clear();
        for (int i = 0; i < groupsTerms.size(); i++)
        {
            for (int j = 0; j < groupsTerms.get(i).size(); j++)
            {
                _rightTerms.add(new Term(groupsTerms.get(i).get(j)));
            }
        }
    }

    ArrayList<ArrayList<Term>> sortInGroups(ArrayList<ArrayList<Term>> groups)
    {
        Term temp;
        ArrayList<Term> groupTemp;

        //Tri Bubblesort dans le groupe.
        for (ArrayList<Term> group : groups)
        {
            for (int j = 0; j < group.size(); j++)
            {
                for (int k = 0; k < (group.size() - j) - 1; k++)
                {
                    //Si l'operateur n'est pas "/" et si l'exposant du terme est plus petit que l'exposant du terme suivant, sinon si la valeur absolue du coefficient du terme est plus petit que la valeur absolue du coefficient du terme suivant.
                    if ((!group.get(k).getOperator().equals("/") || !group.get(k + 1).getOperator().equals("/")) && group.get(k).getExponent() < group.get(k + 1).getExponent() || (group.get(k).getExponent() == group.get(k + 1).getExponent() && Math.abs(group.get(k).getCoefficient()) < Math.abs(group.get(k + 1).getCoefficient())))
                    {
                        //Il ne faut pas que les operateurs bougent, c'est pourquoi on ne les echange pas.
                        temp = new Term(group.get(k));
                        
                        group.remove(k);
                        group.add(k, new Term(group.get(k)));
                        
                        group.remove(k + 1);
                        group.add(k, new Term(temp));
                    }
                }
            }
        }

        //Tri Bubblesort entre les groupes.
        for (int i = 0; i < groups.size(); i++)
        {
            for (int j = 0; j < (groups.size() - i) - 1; j++)
            {
                //Si l'operateur n'est pas "/" et si l'exposant du terme est plus petit que l'exposant du terme suivant, sinon si la valeur absolue du coefficient du terme est plus petit que la valeur absolue du coefficient du terme suivant.
                if ((!groups.get(j + 1).get(0).getOperator().equals("/")) && groups.get(j).get(0).getExponent() < groups.get(j + 1).get(0).getExponent() || (groups.get(j).get(0).getExponent() == groups.get(j + 1).get(0).getExponent() && Math.abs(groups.get(j).get(0).getCoefficient()) < Math.abs(groups.get(j + 1).get(0).getCoefficient())))
                {
                    groupTemp = new ArrayList(groups.get(j));
                    
                    groups.remove(j);
                    groups.add(j, new ArrayList(groups.get(j)));
                    
                    groups.remove(j + 1);
                    groups.add(j + 1, new ArrayList(groupTemp));
                }
            }
        }

        return groups;
    }

    void multiplicationDivision()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et l'operateur est "*".
            if (_leftTerms.get(i).getCharacter().equals(_leftTerms.get(i + 1).getCharacter()) && _leftTerms.get(i + 1).getOperator().equals("*"))
            {
                _leftTerms.get(i).setCoefficient(_leftTerms.get(i).getCoefficient() * _leftTerms.get(i + 1).getCoefficient());	//Multiplie les coefficients.
                _leftTerms.get(i).setExponent(_leftTerms.get(i).getExponent() + _leftTerms.get(i + 1).getExponent());			//Additionne les exposants.

                _leftTerms.get(i + 1).setCoefficient(0);																		//Met le coefficient du deuxieme terme a 0.
                _leftTerms.get(i + 1).setOperator("+");														//Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                break;
            }
            //Si les variables des termes sont les memes et l'operateur est "/".
            else if (_leftTerms.get(i).getCharacter().equals(_leftTerms.get(i + 1).getCharacter()) && _leftTerms.get(i + 1).getOperator().equals("/"))
            {
                _leftTerms.get(i).setCoefficient(_leftTerms.get(i).getCoefficient() / _leftTerms.get(i + 1).getCoefficient());	//Divise les coefficients.
                _leftTerms.get(i).setExponent(_leftTerms.get(i).getExponent() - _leftTerms.get(i + 1).getExponent());			//Soustrait les exposants.

                _leftTerms.get(i + 1).setCoefficient(0);																		//Met le coefficient du deuxieme terme a 0.
                _leftTerms.get(i + 1).setOperator("+");														//Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                break;
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et l'operateur est "*".
            if (_rightTerms.get(i).getCharacter().equals(_rightTerms.get(i + 1).getCharacter()) && _rightTerms.get(i + 1).getOperator().equals("*"))
            {
                _rightTerms.get(i).setCoefficient(_rightTerms.get(i).getCoefficient() * _rightTerms.get(i + 1).getCoefficient());	//Multiplie les coefficients.
                _rightTerms.get(i).setExponent(_rightTerms.get(i).getExponent() + _rightTerms.get(i + 1).getExponent());			//Additionne les exposants.

                _rightTerms.get(i + 1).setCoefficient(0);																		//Met le coefficient du deuxieme terme a 0.
                _rightTerms.get(i + 1).setOperator("+");															//Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                break;
            }
            //Si les variables des termes sont les memes et l'operateur est "/".
            else if (_rightTerms.get(i).getCharacter().equals(_rightTerms.get(i + 1).getCharacter()) && _rightTerms.get(i + 1).getOperator().equals("/"))
            {
                _rightTerms.get(i).setCoefficient(_rightTerms.get(i).getCoefficient() / _rightTerms.get(i + 1).getCoefficient());	//Divise les coefficients.
                _rightTerms.get(i).setExponent(_rightTerms.get(i).getExponent() - _rightTerms.get(i + 1).getExponent());			//Soustrait les exposants.

                _rightTerms.get(i + 1).setCoefficient(0);																		//Met le coefficient du deuxieme terme a 0.
                _rightTerms.get(i + 1).setOperator("+");															//Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                break;
            }
        }

        removeRedundantTerms();

        //Reconstruire l'equation.
        updateEquation("Multiplication/Division");
    }

    void additionSubstraction()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et operateurs sont '+' et les exposants sont les memes.
            if (_leftTerms.get(i).getCharacter().equals(_leftTerms.get(i + 1).getCharacter()) && _leftTerms.get(i).getOperator().equals("+") && _leftTerms.get(i + 1).getOperator().equals("+") && _leftTerms.get(i).getExponent() == _leftTerms.get(i + 1).getExponent())
            {
                _leftTerms.get(i).setCoefficient(_leftTerms.get(i).getCoefficient() + _leftTerms.get(i + 1).getCoefficient());
                _leftTerms.get(i + 1).setCoefficient(0);
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et operateurs sont '+' et les exposants sont les memes.
            if (_rightTerms.get(i).getCharacter().equals(_rightTerms.get(i + 1).getCharacter()) && _rightTerms.get(i).getOperator().equals("+") && _rightTerms.get(i + 1).getOperator().equals("+") && _rightTerms.get(i).getExponent() == _rightTerms.get(i + 1).getExponent())
            {
                _rightTerms.get(i).setCoefficient(_rightTerms.get(i).getCoefficient() + _rightTerms.get(i + 1).getCoefficient());
                _rightTerms.get(i + 1).setCoefficient(0);
            }
        }

        removeRedundantTerms();

        //Reconstruire l'equation.
        updateEquation("Adition/Soustraction");
    }

    void removeRedundantTerms()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //S'il y a plus d'un terme et si le coefficient du terme est 0.
            if (_leftTerms.size() > 1 && _leftTerms.get(i).getCoefficient() == 0)
            {
                _leftTerms.remove(i);
                i--;
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            //S'il y a plus d'un terme et si le coefficient du terme est 0.
            if (_rightTerms.size() > 1 && _rightTerms.get(i).getCoefficient() == 0)
            {
                _rightTerms.remove(i);
                i--;
            }
        }
    }

    void simplify(String etape)
    {
        String lastEquation;

        //Mettre les termes en ordre.
        sortTermsViaGroups();

        //Reconstruire l'equation.
        updateEquation(etape);

        do
        {
            lastEquation = _equation;

            //Appliquer les multiplications et divisions possibles.
            multiplicationDivision();
        }
        //Tant que l'equation peut etre modifiee.
        while (!lastEquation.equals(_equation));

        //Mettre les termes en ordre.
        sortTermsViaGroups();

        //Reconstruire l'equation.
        updateEquation(etape);

        do
        {
            lastEquation = _equation;

            //Appliquer les additions et soustractions possibles.
            additionSubstraction();
        }
        //Tant que l'equation peut etre modifiee.
        while (!lastEquation.equals(_equation));

        //Mettre les termes en ordre.
        sortTermsViaGroups();

        //Reconstruire l'equation.
        updateEquation(etape);
    }

    void mainVariableToLeft()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            if (!_leftTerms.get(i).getCharacter().equals(_variables.get(0)))
            {
                transferTerm(i);
                simplify("Transfert de terme");
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            if (_rightTerms.get(i).getCharacter().equals(_variables.get(0)))
            {
                transferTerm(_leftTerms.size() + i);
                simplify("Transfert de terme");
            }
        }
    }

    void transferTerm(int t)
    {
        Term temp;

        //Si le terme recherche est a gauche du egal.
        if (t < _leftTerms.size())
        {
            temp = new Term(_leftTerms.get(t));
            
            if (temp.getCoefficient() != 0)
            {
                temp.setCoefficient(-1 * temp.getCoefficient());
            }

            _leftTerms.add(new Term(temp));
            _rightTerms.add(new Term(temp));
        }
        else
        {
            temp = new Term(_rightTerms.get(t - _leftTerms.size()));
            
            if (temp.getCoefficient() != 0)
            {
                temp.setCoefficient(-1 * temp.getCoefficient());
            }

            _rightTerms.add(new Term(temp));
            _leftTerms.add(new Term(temp));
        }

        removeRedundantTerms();
    }
    
    void solve()
    {
        //Si les coefficients des premiers termes de chaque cote du egal sont differents de 1.
	if (_leftTerms.get(0).getExponent() != 0 || _rightTerms.get(0).getExponent() != 0)
	{
            mainVariableToLeft();

            //Si l'equation est de degre 1.
            if (_leftTerms.get(0).getExponent() <= 1 && _rightTerms.get(0).getExponent() <= 1)
            {
                solveDegreeOne();
            }
            //Si l'equation est de degre 2.
            else if (_leftTerms.get(0).getExponent() <= 2 && _rightTerms.get(0).getExponent() <= 2)
            {
                solveDegreeTwo();
            }
	}
	else if (_leftTerms.get(0).getCoefficient() != _rightTerms.get(0).getCoefficient())
	{
            System.out.println("Solution impossible\n");
	}
    }

    void solveDegreeOne()
    {
        Term temp = new Term();

        //Transfert du scalaire a droite.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //Si on a un scalaire.
            if (_leftTerms.get(i).getExponent() == 0)
            {
                transferTerm(i);
                simplify("Transfert de terme");
            }
        }

        if (_leftTerms.get(0).getCoefficient() != 1)
        {
            //Division par le coefficient de gauche a droite.
            temp = new Term(_leftTerms.get(0));
            
            temp.setExponent(0);
            temp.setOperator("/");

            _leftTerms.add(new Term(temp));
            _rightTerms.add(new Term(temp));

            simplify("Isoler la variable");
        }
    }

    void solveDegreeTwo()
    {
        String s = "", A = "", B = "", C = "";
        double val1 = 0, val2 = 0, a = 0, b = 0, c = 0;

        for (int i = 0; i < _leftTerms.size(); i++)
        {            
            if (_leftTerms.get(i).getExponent() == 2)
            {
                A = _df.format(_leftTerms.get(i).getCoefficient());
            }
            else if (_leftTerms.get(i).getExponent() == 1)
            {
                B = _df.format(_leftTerms.get(i).getCoefficient());
            }

            if (_leftTerms.get(i).getExponent() == 0)
            {
                C = _df.format(_leftTerms.get(i).getCoefficient());
            }
        }
        
        a = Double.parseDouble(A);
        b = Double.parseDouble(B);
        c = Double.parseDouble(C);

        //Si la racine est positive.
        if (Math.pow(b, 2) - (4 * a * c) >= 0)
        {
            //x1 = (-b + sqrt(b^2 - 4ac)) / 2a
            //x2 = (-b - sqrt(b^2 - 4ac)) / 2a

            val1 = (-1 * b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (a * 2);
            val2 = (-1 * b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (a * 2);
            
            displaySolutionsDegreeTwo(a, b, c, val1, val2);
        }
        else
        {
            System.out.println("Aucune solution reelle\n" + _variables.get(0) + " = " + _df.format((-1 * b) / (a * 2)) + " ± sqrt(" + _df.format(Math.pow(b, 2) - (4 * a * c)) + ") / " + _df.format(a * 2));
        }
    }

    void displaySolutionsDegreeTwo(double a, double b, double c, double sol1, double sol2)
    {
        if (sol1 == sol2)
        {
            System.out.println("Solution unique de l'equation : \n" + _leftTerms.get(0).getCharacter() + " = " + _df.format((-1 * b) / (a * 2)) + " ± " + _df.format(Math.sqrt(Math.pow(b, 2) - (4 * a * c))) + " / " + _df.format(a * 2) + " = " + _df.format(sol1) + "\n");
        }
        else
        {
            System.out.println("Solutions de l'equation : \n" + _leftTerms.get(0).getCharacter() + " = " + _df.format((-1 * b) / (a * 2)) + " ± " + _df.format(Math.sqrt(Math.pow(b, 2) - (4 * a * c)) / (a * 2)) + "\n" + _leftTerms.get(0).getCharacter() + "1 = " + _df.format(sol1) + " ou " + _leftTerms.get(0).getCharacter() + "2 = " + _df.format(sol2) + "\n");
        }
    }
    
    void fillParenthesis()
    {
        int pos = findCharPositionInString("(", _equation, 0), posEqual = findCharPositionInString("=", _equation, 0);
        
        while (pos != 0)
        {
            if (pos < posEqual)
            {
                _leftParenthesis.add(new Parenthesis(pos, true));
            }
            else
            {
                _rightParenthesis.add(new Parenthesis(pos, true));
            }
            
            pos = findCharPositionInString("(", _equation, pos);
        }
        
        pos = findCharPositionInString(")", _equation, 0);
        
        while (pos != 0)
        {
            if (pos < posEqual)
            {
                _leftParenthesis.add(new Parenthesis(pos, false));
            }
            else
            {
                _rightParenthesis.add(new Parenthesis(pos, false));
            }
            
            pos = findCharPositionInString(")", _equation, pos);
        }
    }

    void parenthesis()
    {
        fillParenthesis();
        
        //S'il y a des parentheses.
        if (!_leftParenthesis.isEmpty() && !_rightParenthesis.isEmpty())
        {
            //Gauche du egal.
            for (int i = 0; i < _leftParenthesis.size(); i++)
            {
                //Si l'operateur devant la parenthese ouvrante n'est pas "*" ou "/" et si l'operateur apres la parenthese fermante n'est pas "*" ou "/".
                if (_equation.charAt(posNextOpen - 2) != '*' && _equation.charAt(posNextOpen - 2) != '/' && _equation.charAt(posNextClose) != '*' && _equation.charAt(posNextClose) != '/')
                {
                    //Enleve les parentheses.
                    _equation = _equation.substring(0, posNextOpen - 1) + _equation.substring(posNextOpen, (posNextClose - 1) - posNextOpen) + _equation.substring(posNextClose, _equation.length());
                }
                //Si l'operateur devant la parenthese ouvrante est "*".
                else if (_equation.charAt(posNextOpen - 2) == '*' || _equation.charAt(posNextOpen - 2) == '/')
                {
                    //Gauche du egal.
                    for (int i = 0; i < _leftTerms.size(); i++)
                    {
                        //Si le terme est compris dans les parentheses.
                        if (posNextOpen < _leftTerms.get(i).getPosition() && _leftTerms.get(i).getPosition() < posNextClose)
                        {
                            nbrTerms++;
                        }
                    }
                    //Droite du egal.
                    for (int i = 0; i < _rightTerms.size(); i++)
                    {
                        //Si le terme est compris dans les parentheses.
                        if (posNextOpen < _rightTerms.get(i).getPosition() && _rightTerms.get(i).getPosition() < posNextClose)
                        {
                            nbrTerms++;
                        }
                    }

                    //S'il y a plus d'un terme dans les parentheses.
                    if (nbrTerms > 1)
                    {
                        //Enleve les parentheses.
                        _equation = _equation.substring(0, posNextOpen - 1) + _equation.substring(posNextOpen, (posNextClose - 1) - posNextOpen) + _equation.substring(posNextClose, _equation.length());
                    }
                    else
                    {
                        //Enleve les parentheses.
                        _equation = _equation.substring(0, posNextOpen - 1) + _equation.substring(posNextOpen, (posNextClose - 1) - posNextOpen) + _equation.substring(posNextClose, _equation.length());
                    }
                }
            }
        }
    }

    void updateEquation(String etape)
    {
        String lastEquation = _equation;
        _equation = "";                                         //Efface l'equation

        //Gauche du egal.
        int index = 0;
        while (_leftTerms.size() > index)			//S'il y a un prochain terme.
        {
            _equation += _leftTerms.get(index).display();       //Affiche le prochain terme et son operateur devant.
            index++;
        }

        _equation += "=";					//Rajoute le egal.

        //Droite du egal.
        index = 0;
        while (_rightTerms.size() > index)			//S'il y a un prochain terme.
        {
            _equation += _rightTerms.get(index).display();      //Affiche le prochain terme et son operateur devant.
            index++;
        }

        if (!_equation.equals(lastEquation) || etape.equals("Equation recue"))
        {
            displayEquation(etape);
        }
    }

    void displayEquation(String etape)
    {        
        String equation = "";

        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            if (i != 0)
            {
                equation += " " + _leftTerms.get(i).getOperator() + " ";
            }

            if (_leftTerms.get(i).getExponent() != 0)
            {                
                if (Math.abs(_leftTerms.get(i).getCoefficient()) == 1 && _leftTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else if (Math.abs(_leftTerms.get(i).getCoefficient()) != 1)
                {                    
                    equation += _df.format(_leftTerms.get(i).getCoefficient());
                }

                if (_leftTerms.get(i).getCoefficient() != 0)
                {
                    equation += _leftTerms.get(i).getCharacter();
                }

                if (_leftTerms.get(i).getExponent() != 1)
                {
                    equation += "^";
                    equation += _df.format(_leftTerms.get(i).getExponent());
                }
            }
            else
            {
                equation += _df.format(_leftTerms.get(i).getCoefficient());
            }
        }

        equation += " = ";

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            if (i != 0)
            {
                equation += " " + _rightTerms.get(i).getOperator() + " ";
            }

            if (_rightTerms.get(i).getExponent() != 0)
            {
                if (Math.abs(_rightTerms.get(i).getCoefficient()) == 1 && _rightTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else if (Math.abs(_rightTerms.get(i).getCoefficient()) != 1)
                {                    
                    equation += _df.format(_rightTerms.get(i).getCoefficient());
                }

                if (_rightTerms.get(i).getCoefficient() != 0)
                {
                    equation += _rightTerms.get(i).getCharacter();
                }

                if (_rightTerms.get(i).getExponent() != 1)
                {
                    equation += "^";
                    equation += _df.format(_rightTerms.get(i).getExponent());
                }
            }
            else
            {
                equation += _df.format(_rightTerms.get(i).getCoefficient());
            }
        }

        //Pour l'alignement.
        if (_equationLegthDisplay == 0)
        {
            _equationLegthDisplay = equation.length();
        }

        System.out.println(equation + " (" + etape + ")\n");
    }
    
    public static void main(String arcs[])
    {
        String equation = "+(+4*x^0+6*x^0)=+1*x^1";

	ArrayList<String> variables = new ArrayList();
	variables.add("x");

	Resolution resolution = new Resolution(equation, variables);
        
        resolution.fillTerms();
	resolution.parenthesis();   //Pas Fini
	resolution.simplify("Ordonner");
        resolution.solve();
    }
}

//DecimalFormat: http://stackoverflow.com/questions/14204905/java-how-to-remove-trailing-zeros-from-a-double