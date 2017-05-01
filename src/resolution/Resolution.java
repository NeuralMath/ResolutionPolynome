package resolution;

import java.util.ArrayList;
import java.text.DecimalFormat;

class Resolution
{
    String _equation;                           //L'equation a resoudre.
    ArrayList<String> _variables;               //Les variables dans l'equation.
    ArrayList<Term> _leftTerms;                 //Les termes de l'equation a gauche du egal.
    ArrayList<Term> _rightTerms;                //Les termes de l'equation a droite du egal.
    int _equationLegthDisplay;                  //La grandeur de l'equation dans l'affichage. Sert uniquement a l'affichage.
    
    DecimalFormat _df = new DecimalFormat("0.##");  //Format de l'affichage.
    
    Resolution(String equation, ArrayList<String> var)
    {
	_equation = equation;
        
        _variables = new ArrayList(var);
        
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

    void fillAllTerms()
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
                        temp = fillTerm(posNextVar, _equation);
                    }
                }

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
    }

    Term fillTerm(int pos, String s)//Permettre les parentheses multiples.
    {
        double coeff = 0, exp = 0;
        String temp = "", op = "";
        ArrayList<String> open = new ArrayList(), close = new ArrayList();

        //Trouve le coefficient du terme.
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
        coeff = Double.parseDouble(temp);
        
        //Trouve l'exposant du terme.
        temp = "";
        int index = pos + 1;
        //Si l'exposant est negatif.
        if (s.charAt(pos + 1) == '-')
        {
            temp += "-";
            index++;        //Commence au prochain caractere.
        }
        for (; index < s.length(); index++)  //Avance de 1 position apres la variable puis ramasse tous les chiffres.
        {            
            if (Character.isDigit(s.charAt(index)))
            {
                temp = temp + s.charAt(index);
            }
            else
            {
                break;
            }
        }
        exp = Double.parseDouble(temp);
        
        //Trouve l'operateur du terme.
        op = "" + s.charAt(pos - 3 - _df.format(coeff).length());   //Recule de 2 positions avant la variable et du nombre de chiffres du coefficient.

        //Trouve l'operateur devant la parenthese ouvrante s'il y en a une devant le terme. Recommence tant qu'il y a des parentheses devant.
        for (int i = pos - 3 - _df.format(coeff).length() - 1; i > 0; i--)   //Recule de 2 positions avant la variable et du nombre de chiffres du coefficient.
        {
            if(s.charAt(i) == '(')
            {
                open.add("" + s.charAt(i - 1));
            }
            else
            {
                break;
            }
        }
        
        //Determine s'il y a une parenthese fermante apres le terme. Recommence tant qu'il y a des parentheses apres.
        for (int i = pos + 1 + _df.format(exp).length(); i < s.length(); i++)   //Avance de 1 position apres la variable et du nombre de chiffres d'exposant.
        {
            if(s.charAt(i) == ')')
            {
                close.add("0");
            }
            else
            {
                break;
            }
        }
        
        return new Term(pos, "" + s.charAt(pos - 1), coeff, exp, op, open, close);
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
    
    String simplify(ArrayList<Term> terms)
    {
        String  etape = "";
        
        etape = multiplicationDivision(terms);
        
        if (etape.equals(""))
        {
            etape = additionSubstraction(terms);
        }
        
        return etape;
    }

    ArrayList<Term> sortTermsViaGroups(ArrayList<Term> terms)
    {
        ArrayList<Term> termsTemp = new ArrayList();
        ArrayList<ArrayList<Term>> groupsTerms = new ArrayList();

        for (int i = 0; i < terms.size(); i++)
        {
            //Si le groupe est vide ou que l'operateur du terme n'est pas un "*" ou un "/".
            if (termsTemp.isEmpty() || !terms.get(i).getOperator().equals("+"))
            {
                termsTemp.add(new Term(terms.get(i)));              //Copie le dans le groupe.
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

        //Copie groupsTerms dans terms.
        terms.clear();
        for (int i = 0; i < groupsTerms.size(); i++)
        {
            for (int j = 0; j < groupsTerms.get(i).size(); j++)
            {
                terms.add(new Term(groupsTerms.get(i).get(j)));
            }
        }
        
        return terms;
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
        
        /*
        ArrayList<Term> t = new ArrayList();
        MergeSort ms = new MergeSort();
        
        //Tri MergeSort dans les groupes.
        for (ArrayList<Term> group : groups)
        {
            for (int i = 0; i < group.size(); i++)
            {
                //t.remove(i);
                t.add(new Term(group.get(i)));
            }
            
            ms.sort(t);
            
            for (Term temp : t)
            {
                group.clear();
                group.add(new Term(temp));
            }
        }
        
        //Tri MergeSort entre les groupes.
        for (ArrayList<Term> group : groups)
        {
            for (int i = 0; i < group.size(); i++)
            {
                t.remove(i);
                t.add(i, new Term(group.get(0)));
            }
            
            ms.sort(t);
            
            for (Term temp : t)
            {
                group.clear();
                group.add(new Term(temp));
            }
        }
        */
        
        return groups;
    }

    String multiplicationDivision(ArrayList<Term> terms)
    {
        ArrayList<Term> termsTemp = terms;
        
        for (int i = 0; i < terms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et l'operateur est "*".
            if (terms.get(i).getCharacter().equals(terms.get(i + 1).getCharacter()) && terms.get(i + 1).getOperator().equals("*"))
            {
                terms.get(i).setCoefficient(terms.get(i).getCoefficient() * terms.get(i + 1).getCoefficient());                         //Multiplie les coefficients.
                terms.get(i).setExponent(terms.get(i).getExponent() + terms.get(i + 1).getExponent());                                  //Aditionne les exposants.
                
                for (String close : terms.get(i + 1).getCloseParenthesis())
                {
                    terms.get(i).getCloseParenthesis().add(close);                                                                      //Ajoute les parentheses fermantes.
                }

                terms.get(i + 1).setCoefficient(0);                                                                                     //Met le coefficient du deuxieme terme a 0.
                terms.get(i + 1).setOperator("+");                                                                                      //Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                
                terms = removeRedundantTerms(terms);
                
                return "Multiplication/Division";
            }
            //Si les variables des termes sont les memes et l'operateur est "/".
            else if (terms.get(i).getCharacter().equals(terms.get(i + 1).getCharacter()) && terms.get(i + 1).getOperator().equals("/"))
            {
                terms.get(i).setCoefficient(terms.get(i).getCoefficient() / terms.get(i + 1).getCoefficient());                         //Divise les coefficients.
                terms.get(i).setExponent(terms.get(i).getExponent() - terms.get(i + 1).getExponent());                                  //Soustrait les exposants.
                
                for (String close : terms.get(i + 1).getCloseParenthesis())
                {
                    terms.get(i).getCloseParenthesis().add(close);                                                                      //Ajoute les parentheses fermantes.
                }

                terms.get(i + 1).setCoefficient(0);                                                                                     //Met le coefficient du deuxieme terme a 0.
                terms.get(i + 1).setOperator("+");                                                                                      //Met l'operateur du terme a "+" pour qu'il ne le multiplie pas encore.
                
                terms = removeRedundantTerms(terms);
                
                return "Multiplication/Division";
            }
        }
        return "";
    }

    String additionSubstraction(ArrayList<Term> terms)
    {
        ArrayList<Term> termsTemp = terms;
        
        for (int i = 0; i < terms.size() - 1; i++)
        {
            //Si les variables des termes sont les memes et operateurs sont '+' et les exposants sont les memes.
            if (terms.get(i).getCharacter().equals(terms.get(i + 1).getCharacter()) && terms.get(i).getOperator().equals("+") && terms.get(i + 1).getOperator().equals("+") && terms.get(i).getExponent() == terms.get(i + 1).getExponent())
            {
                terms.get(i).setCoefficient(terms.get(i).getCoefficient() + terms.get(i + 1).getCoefficient());     //Aditionne les coefficients.
                
                for (String close : terms.get(i + 1).getCloseParenthesis())
                {
                    terms.get(i).getCloseParenthesis().add(close);                                                  //Ajoute les parentheses fermantes.
                }

                terms.get(i + 1).setCoefficient(0);
                terms.get(i + 1).setOperator("+");
                
                terms = removeRedundantTerms(terms);
                
                return "Adition/Soustraction";
            }
        }
        
        return "";
    }

    ArrayList<Term> removeRedundantTerms(ArrayList<Term> terms)
    {
        for (int i = 0; i < terms.size(); i++)
        {
            //S'il y a plus d'un terme et si le coefficient du terme est 0.
            if (terms.size() > 1 && terms.get(i).getCoefficient() == 0)
            {
                terms.remove(i);
                i--;
            }
        }
        
        return terms;
    }
    
    void solve()
    {
        String etape = "";
        
        do
        {            
            etape = simplify(_leftTerms);
            updateEquation(etape);
        }
        while(!etape.equals(""));
        
        _leftTerms = sortTermsViaGroups(_leftTerms);
        updateEquation("Ordonner");
        
        do
        {            
            etape = simplify(_rightTerms);
            updateEquation(etape);
        }
        while(!etape.equals(""));
        
        _rightTerms = sortTermsViaGroups(_rightTerms);
        updateEquation("Ordonner");
        
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
        String etape = "";
        Term temp = new Term();

        //Transfert du scalaire a droite.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //Si on a un scalaire.
            if (_leftTerms.get(i).getExponent() == 0)
            {
                transferTerm(i);
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
            updateEquation("Transfert de terme");

            do
            {
                etape = simplify(_leftTerms);
                updateEquation(etape);
            }
            while(!etape.equals(""));

            do
            {
                etape = simplify(_rightTerms);
                updateEquation(etape);
            }
            while(!etape.equals(""));
        }
    }
    
    void mainVariableToLeft()
    {
        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            if (!_leftTerms.get(i).getCharacter().equals(_variables.get(0)) && (_leftTerms.size() != 1 || _leftTerms.get(0).getCoefficient() != 0))
            {
                transferTerm(i);
                i--;
            }
        }

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            if (_rightTerms.get(i).getCharacter().equals(_variables.get(0)) && (_rightTerms.size() != 1 || _rightTerms.get(0).getCoefficient() != 0))
            {
                transferTerm(_leftTerms.size() + i);
                i--;
            }
        }
    }
    
    void transferTerm(int t)
    {
        String etape = "";
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
        removeRedundantTerms(_leftTerms);
        removeRedundantTerms(_rightTerms);
        updateEquation("Transfert de terme");
        
        _leftTerms = sortTermsViaGroups(_leftTerms);
        _rightTerms = sortTermsViaGroups(_rightTerms);
        updateEquation("Ordonner");
        
        do
        {
            etape = simplify(_leftTerms);
            updateEquation(etape);
        }
        while(!etape.equals(""));

        do
        {
            etape = simplify(_rightTerms);
            updateEquation(etape);
        }
        while(!etape.equals(""));
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

    void manageParenthesis()
    {
        updateEquation("Equation recue");
        
        String lastEquation = "", etape = "";
        ArrayList<Term> termsInParenthesis = new ArrayList();
        ArrayList<Term> termsTemp;
        ArrayList<Term> groupPriority = new ArrayList();
        
        //Enlever toutes les parentheses possibles.
        while(parenthesisLeft() && !lastEquation.equals(_equation))
        {
            lastEquation = _equation;
            
            //Gauche du egal.
            for (int i = 0; i < _leftTerms.size(); i++)
            {
                //Enleve les parentheses les plus proches du terme tant qu'il y en a en trop.
                while (!_leftTerms.get(i).getOpenParenthesis().isEmpty() && !_leftTerms.get(i).getCloseParenthesis().isEmpty())
                {
                    _leftTerms.get(i).setOperator(_leftTerms.get(i).getOpenParenthesis().get(0));
                    
                    _leftTerms.get(i).getOpenParenthesis().remove(0);
                    _leftTerms.get(i).getCloseParenthesis().remove(0);
                }
                
                updateEquation("Enlever parentheses");
                    
                //S'il y a une parenthese ouvrante devant le terme.
                if (!_leftTerms.get(i).getOpenParenthesis().isEmpty())
                {                    
                    termsInParenthesis.clear();
                    termsInParenthesis.add(new Term(_leftTerms.get(i)));
                }
                //Sinon s'il y a deja un terme entre parentheses.
                else if (!termsInParenthesis.isEmpty())
                {                    
                    termsInParenthesis.add(new Term(_leftTerms.get(i)));
                    
                    //Si le terme a une parenthese fermante.
                    if (!_leftTerms.get(i).getCloseParenthesis().isEmpty())
                    {
                        termsTemp = new ArrayList(termsInParenthesis);
                        etape = simplify(termsInParenthesis);
                        
                        //Si les termes entre parentheses ont pu etre simplifies.
                        if (termsTemp.size() != termsInParenthesis.size())
                        {
                            for (int j = 0; j < termsInParenthesis.size(); j++)
                            {
                                _leftTerms.add(i - 1, new Term(termsInParenthesis.get(j)));
                                _leftTerms.remove(i - j);
                            }

                            i = i - termsInParenthesis.size() - 1;
                            termsInParenthesis.clear();
                        }
                        else if (termsInParenthesis.get(0).getOpenParenthesis().get(0).equals("+"))
                        {
                            if (i + 1 <= _leftTerms.size() - 1)
                            {
                                if (!_leftTerms.get(i + 1).getOpenParenthesis().isEmpty() && _leftTerms.get(i + 1).getOpenParenthesis().get(_leftTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("+") || _leftTerms.get(i + 1).getOpenParenthesis().isEmpty() && _leftTerms.get(i + 1).getOperator().equals("+"))
                                {
                                    for (int j = i - (termsInParenthesis.size() - 1); j < i + 1; j++)
                                    {
                                        _leftTerms.get(j).setOperator(_leftTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().get(0));
                                    }
                                    _leftTerms.get(i).getCloseParenthesis().remove(0);
                                    _leftTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().remove(0);

                                    termsInParenthesis.clear();
                                    etape = "Enlever parentheses";
                                }
                                else if (!_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && (_rightTerms.get(i + 1).getOpenParenthesis().get(_rightTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("*") || _rightTerms.get(i + 1).getOpenParenthesis().get(_rightTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("/")) || (_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOperator().equals("*") || _rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOperator().equals("/")))
                                {
                                    //Si le terme suivant a au moins une parenthese ouvrante devant.
                                    if (!_leftTerms.get(i + 1).getOpenParenthesis().isEmpty())
                                    {
                                        for (int j = i + 1; j < _leftTerms.size(); j++)
                                        {
                                            groupPriority.add(new Term(_leftTerms.get(j)));

                                            if (!_leftTerms.get(j).getCloseParenthesis().isEmpty())
                                            {
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        groupPriority.add(new Term(_leftTerms.get(i + 1)));
                                    }

                                    termsTemp = distribute(termsInParenthesis, groupPriority);
                                    etape = "Distributivite";

                                    if ((i + 1) + groupPriority.size() < _leftTerms.size())
                                    {
                                        _leftTerms.subList((i + 1) - termsInParenthesis.size(), (i + 1) + groupPriority.size()).clear();
                                    }
                                    else
                                    {
                                        _leftTerms.subList((i + 1) - termsInParenthesis.size(), _leftTerms.size()).clear();
                                    }

                                    for (int j = 0; j < termsTemp.size(); j++)
                                    {
                                        _leftTerms.add((i + 1 + j) - termsInParenthesis.size(), new Term(termsTemp.get(j)));
                                    }
                                }
                                else if (_leftTerms.get(i + 1).getOpenParenthesis().isEmpty() && _leftTerms.get(i + 1).getCoefficient() == Math.floor(_leftTerms.get(i + 1).getCoefficient()) && _leftTerms.get(i + 1).getCoefficient() > 0)
                                {
                                    //Exposants.
                                    termsTemp = new ArrayList(termsInParenthesis);
                                    termsTemp.get(0).getOpenParenthesis().remove(termsTemp.get(0).getOpenParenthesis().size() - 1);
                                    termsTemp.get(0).getOpenParenthesis().add(termsTemp.get(0).getOpenParenthesis().size() - 1, "*");
                                        
                                    for (int j = 0; j < _leftTerms.get(i + 1).getCoefficient() - 1; j++)
                                    {                                                
                                        for (int k = 0; k < termsTemp.size(); k++)
                                        {
                                            _leftTerms.add((i + 1 + k + (termsTemp.size() * j)), new Term(termsTemp.get(k)));
                                        }
                                    }
                                    
                                    _leftTerms.remove(i + 1 + (termsInParenthesis.size() * (int)_leftTerms.get(i + 1).getCoefficient() - 1));
                                }
                            }
                            else
                            {
                                for (int j = i - (termsInParenthesis.size() - 1); j < i + 1; j++)
                                {
                                    _leftTerms.get(j).setOperator(_leftTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().get(0));
                                }
                                _leftTerms.get(i).getCloseParenthesis().remove(0);
                                _leftTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().remove(0);

                                termsInParenthesis.clear();
                                etape = "Enlever parentheses";
                            }
                        }
                        else
                        {
                            //Si le terme precedant a au moins une parenthese fermante apres.
                            if (!_leftTerms.get(i - termsInParenthesis.size()).getCloseParenthesis().isEmpty())
                            {
                                for (int j = i - termsInParenthesis.size(); j < 0; j--)
                                {
                                    groupPriority.add(new Term(_leftTerms.get(j)));

                                    if (!_leftTerms.get(j).getOpenParenthesis().isEmpty())
                                    {
                                        break;
                                    }
                                }

                                //On remet les termes en ordre dans groupPriority
                                for(int j = 0; j < groupPriority.size() - 1; j++)
                                {
                                    groupPriority.add(groupPriority.size() - j, new Term(groupPriority.get(j)));
                                    groupPriority.remove(j);
                                }
                            }
                            else
                            {
                                groupPriority.add(new Term(_leftTerms.get(i - termsInParenthesis.size())));
                            }

                            termsTemp = distribute(groupPriority, termsInParenthesis);
                            etape = "Distributivite";

                            if (i + 1 < _leftTerms.size())
                            {
                                _leftTerms.subList((i + 1) - groupPriority.size() - termsInParenthesis.size(), i + 1).clear();
                            }
                            else
                            {
                                _leftTerms.subList((i + 1) - groupPriority.size() - termsInParenthesis.size(), _leftTerms.size()).clear();
                            }

                            for (int j = 0; j < termsTemp.size(); j++)
                            {
                                _leftTerms.add((i + 1 + j) - groupPriority.size() - termsInParenthesis.size(), new Term(termsTemp.get(j)));
                            }
                        }
                    }
                }
                
                updateEquation(etape);
            }
            
            //Droite du egal.
            for (int i = 0; i < _rightTerms.size(); i++)
            {
                //Enleve les parentheses les plus proches du terme tant qu'il y en a en trop.
                while (!_rightTerms.get(i).getOpenParenthesis().isEmpty() && !_rightTerms.get(i).getCloseParenthesis().isEmpty())
                {
                    _rightTerms.get(i).setOperator(_rightTerms.get(i).getOpenParenthesis().get(0));
                    
                    _rightTerms.get(i).getOpenParenthesis().remove(0);
                    _rightTerms.get(i).getCloseParenthesis().remove(0);
                }
                
                updateEquation("Enlever parentheses");
                    
                //S'il y a une parenthese ouvrante devant le terme.
                if (!_rightTerms.get(i).getOpenParenthesis().isEmpty())
                {                    
                    termsInParenthesis.clear();
                    termsInParenthesis.add(new Term(_rightTerms.get(i)));
                }
                //Sinon s'il y a deja un terme entre parentheses.
                else if (!termsInParenthesis.isEmpty())
                {                    
                    termsInParenthesis.add(new Term(_rightTerms.get(i)));
                    
                    //Si le terme a une parenthese fermante.
                    if (!_rightTerms.get(i).getCloseParenthesis().isEmpty())
                    {
                        termsTemp = new ArrayList(termsInParenthesis);
                        etape = simplify(termsInParenthesis);
                        
                        //Si les termes entre parentheses ont pu etre simplifies.
                        if (termsTemp.size() != termsInParenthesis.size())
                        {
                            for (int j = 0; j < termsInParenthesis.size(); j++)
                            {
                                _rightTerms.add(i - 1, new Term(termsInParenthesis.get(j)));
                                _rightTerms.remove(i - j);
                            }

                            i = i - termsInParenthesis.size() - 1;
                            termsInParenthesis.clear();
                        }
                        else if (termsInParenthesis.get(0).getOpenParenthesis().get(0).equals("+"))
                        {
                            if (i + 1 <= _rightTerms.size() - 1)
                            {
                                if (!_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOpenParenthesis().get(_rightTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("+") || _rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOperator().equals("+"))
                                {
                                    for (int j = i - (termsInParenthesis.size() - 1); j < i + 1; j++)
                                    {
                                        _rightTerms.get(j).setOperator(_rightTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().get(0));
                                    }
                                    _rightTerms.get(i).getCloseParenthesis().remove(0);
                                    _rightTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().remove(0);

                                    termsInParenthesis.clear();
                                    etape = "Enlever parentheses";
                                }
                                else if (!_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && (_rightTerms.get(i + 1).getOpenParenthesis().get(_rightTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("*") || _rightTerms.get(i + 1).getOpenParenthesis().get(_rightTerms.get(i + 1).getOpenParenthesis().size() - 1).equals("/")) || (_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOperator().equals("*") || _rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getOperator().equals("/")))
                                {
                                    //Si le terme suivant a au moins une parenthese ouvrante devant.
                                    if (!_rightTerms.get(i + 1).getOpenParenthesis().isEmpty())
                                    {
                                        for (int j = i + 1; j < _rightTerms.size(); j++)
                                        {
                                            groupPriority.add(new Term(_rightTerms.get(j)));

                                            if (!_rightTerms.get(j).getCloseParenthesis().isEmpty())
                                            {
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        groupPriority.add(new Term(_rightTerms.get(i + 1)));
                                    }

                                    termsTemp = distribute(termsInParenthesis, groupPriority);
                                    etape = "Distributivite";

                                    if ((i + 1) + groupPriority.size() < _rightTerms.size())
                                    {
                                        _rightTerms.subList((i + 1) - termsInParenthesis.size(), (i + 1) + groupPriority.size()).clear();
                                    }
                                    else
                                    {
                                        _rightTerms.subList((i + 1) - termsInParenthesis.size(), _rightTerms.size()).clear();
                                    }

                                    for (int j = 0; j < termsTemp.size(); j++)
                                    {
                                        _rightTerms.add((i + 1 + j) - termsInParenthesis.size(), new Term(termsTemp.get(j)));
                                    }
                                }
                                else if (_rightTerms.get(i + 1).getOpenParenthesis().isEmpty() && _rightTerms.get(i + 1).getCoefficient() == Math.floor(_rightTerms.get(i + 1).getCoefficient()) && _rightTerms.get(i + 1).getCoefficient() > 0)
                                {
                                    //Exposants.
                                    termsInParenthesis.get(0).getOpenParenthesis().remove(termsInParenthesis.get(0).getOpenParenthesis().size() - 1);
                                    termsInParenthesis.get(0).getOpenParenthesis().add("*");
                                    
                                    for (int j = 0; j < _rightTerms.get(i + 1).getCoefficient() - 1; j++)
                                    {
                                        _rightTerms.addAll((i + 1 + (termsTemp.size() * j)), new ArrayList(termsInParenthesis));
                                    }
                                    
                                    _rightTerms.remove(i + 1 + (termsInParenthesis.size() * (int)_rightTerms.get(i + 1).getCoefficient()));
                                    
                                    etape = "Exposant";
                                }
                            }
                            else
                            {
                                for (int j = i - (termsInParenthesis.size() - 1); j < i + 1; j++)
                                {
                                    _rightTerms.get(j).setOperator(_rightTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().get(0));
                                }
                                _rightTerms.get(i).getCloseParenthesis().remove(0);
                                _rightTerms.get(i - (termsInParenthesis.size() - 1)).getOpenParenthesis().remove(0);

                                termsInParenthesis.clear();
                                etape = "Enlever parentheses";
                            }
                        }
                        else
                        {
                            //Si le terme precedant a au moins une parenthese fermante apres.
                            if (!_rightTerms.get(i - termsInParenthesis.size()).getCloseParenthesis().isEmpty())
                            {
                                for (int j = i - termsInParenthesis.size(); j >= 0; j--)
                                {
                                    groupPriority.add(new Term(_rightTerms.get(j)));

                                    if (!_rightTerms.get(j).getOpenParenthesis().isEmpty())
                                    {
                                        break;
                                    }
                                }

                                //On remet les termes en ordre dans groupPriority
                                for(int j = 0; j < groupPriority.size() - 1; j++)
                                {
                                    groupPriority.add(groupPriority.size() - j, new Term(groupPriority.get(j)));
                                    groupPriority.remove(j);
                                }
                            }
                            else
                            {
                                groupPriority.add(new Term(_rightTerms.get(i - termsInParenthesis.size())));
                            }

                            termsTemp = distribute(groupPriority, termsInParenthesis);
                            etape = "Distributivite";

                            if (i + 1 < _rightTerms.size())
                            {
                                _rightTerms.subList((i + 1) - groupPriority.size() - termsInParenthesis.size(), i + 1).clear();
                            }
                            else
                            {
                                _rightTerms.subList((i + 1) - groupPriority.size() - termsInParenthesis.size(), _rightTerms.size()).clear();
                            }

                            for (int j = 0; j < termsTemp.size(); j++)
                            {
                                _rightTerms.add((i + 1 + j) - groupPriority.size() - termsInParenthesis.size(), new Term(termsTemp.get(j)));
                            }
                        }
                    }
                }
                
                updateEquation(etape);
            }
        }
    }
    
    boolean parenthesisLeft()
    {        
        for (Term term : _leftTerms)
        {
            if (!term.getOpenParenthesis().isEmpty())
            {
                return true;
            }
        }
        
        for (Term term : _rightTerms)
        {
            if (!term.getOpenParenthesis().isEmpty())
            {
                return true;
            }
        }
        
        return false;
    }
    
    ArrayList<Term> distribute(ArrayList<Term> terms1, ArrayList<Term> terms2)
    {
        ArrayList<ArrayList<Term>> groups = new ArrayList();
        ArrayList<Term> temp = new ArrayList();
        
        if (!terms1.get(0).getOpenParenthesis().isEmpty() && !terms1.get(terms1.size() - 1).getCloseParenthesis().isEmpty())
        {
            for (int i = 0; i < terms1.size(); i++)
            {
                terms1.get(i).setOperator(terms1.get(0).getOpenParenthesis().get(terms1.get(0).getOpenParenthesis().size() - 1));
            }
            
            terms1.get(0).getOpenParenthesis().remove(terms1.get(0).getOpenParenthesis().size() - 1);
            terms1.get(terms1.size() - 1).getCloseParenthesis().remove(terms1.get(terms1.size() - 1).getCloseParenthesis().size() - 1);
        }
        
        if (!terms2.get(0).getOpenParenthesis().isEmpty() && !terms2.get(terms2.size() - 1).getCloseParenthesis().isEmpty())
        {
            for (int i = 0; i < terms2.size(); i++)
            {
                terms2.get(i).setOperator(terms2.get(0).getOpenParenthesis().get(terms2.get(0).getOpenParenthesis().size() - 1));
            }
            
            terms2.get(0).getOpenParenthesis().remove(terms2.get(0).getOpenParenthesis().size() - 1);
            terms2.get(terms2.size() - 1).getCloseParenthesis().remove(terms2.get(terms2.size() - 1).getCloseParenthesis().size() - 1);
        }
        
        for (int i = 0; i < terms1.size(); i++)
        {
            for (int j = 0; j < terms2.size(); j++)
            {
                temp.add(new Term(terms1.get(i)));
                temp.add(new Term(terms2.get(j)));

                groups.add(new ArrayList(temp));
                temp.clear();
            }
        }
        
        for (ArrayList<Term> terms : groups)
        {
            for (Term term : terms)
            {
                temp.add(new Term(term));
            }
        }
        
        return temp;
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
        
        //Affichage de l'equation.
        if (!_equation.equals(lastEquation) || etape.equals("Equation recue"))
        {
            displayEquation(etape);
        }
    }

    /*void displayEquation(String etape)
    {
        String equation = "";

        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //S'il y a une parenthese ouvrante avant le terme.
            if (!_leftTerms.get(i).getOpenParenthesis().isEmpty())
            {
                for (String open : _leftTerms.get(i).getOpenParenthesis())
                {
                    equation += (i != 0 ? (" " + open + " ") : "") + "(";
                }
            }
            
            if (i != 0 && equation.charAt(equation.length() - 1) != '(')
            {
                equation += " ";
                
                if (_leftTerms.get(i).getOperator().equals("+") && _leftTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else
                {
                    equation += _leftTerms.get(i).getOperator();
                }
                
                equation += " ";
            }

            if (_leftTerms.get(i).getExponent() != 0)
            {
                if (Math.abs(_leftTerms.get(i).getCoefficient()) != 1)
                {
                    if (i != 0 && _leftTerms.get(i).getOperator().equals("+"))
                    {
                        equation += _df.format(Math.abs(_leftTerms.get(i).getCoefficient()));
                    }
                    else
                    {
                        equation += _df.format(_leftTerms.get(i).getCoefficient());
                    }
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
                if (i != 0 && _leftTerms.get(i).getOperator().equals("+"))
                {
                    equation += _df.format(Math.abs(_leftTerms.get(i).getCoefficient()));
                }
                else
                {
                    equation += _df.format(_leftTerms.get(i).getCoefficient());
                }
            }
            
            //S'il y a une parenthese fermante apres le terme.
            if (!_leftTerms.get(i).getCloseParenthesis().isEmpty())
            {
                for (String close : _leftTerms.get(i).getCloseParenthesis())
                {
                    equation += ")";
                }
            }
        }

        equation += " = ";

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            //S'il y a une parenthese ouvrante avant le terme.
            if (!_rightTerms.get(i).getOpenParenthesis().isEmpty())
            {
                for (String open : _rightTerms.get(i).getOpenParenthesis())
                {
                    equation += (i != 0 ? (" " + open + " ") : "") + "(";
                }
            }
            
            if (i != 0 && equation.charAt(equation.length() - 1) != '(')
            {
                equation += " ";
                
                if (_rightTerms.get(i).getOperator().equals("+") && _rightTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else
                {
                    equation += _rightTerms.get(i).getOperator();
                }
                
                equation += " ";
            }

            if (_rightTerms.get(i).getExponent() != 0)
            {
                if (Math.abs(_rightTerms.get(i).getCoefficient()) != 1)
                {
                    if (i != 0 && _rightTerms.get(i).getOperator().equals("+"))
                    {
                        equation += _df.format(Math.abs(_rightTerms.get(i).getCoefficient()));
                    }
                    else
                    {
                        equation += _df.format(_rightTerms.get(i).getCoefficient());
                    }
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
                if (i != 0 && _rightTerms.get(i).getOperator().equals("+"))
                {
                    equation += _df.format(Math.abs(_rightTerms.get(i).getCoefficient()));
                }
                else
                {
                    equation += _df.format(_rightTerms.get(i).getCoefficient());
                }
            }
            
            //S'il y a une parenthese fermante apres le terme.
            if (!_rightTerms.get(i).getCloseParenthesis().isEmpty())
            {
                for (String close : _rightTerms.get(i).getCloseParenthesis())
                {
                    equation += ")";
                }
            }
        }

        //Pour l'alignement.
        if (_equationLegthDisplay == 0)
        {
            _equationLegthDisplay = equation.length();
        }
        
        System.out.println(equation + " (" + etape + ")\n");
    }
    */
    
    void displayEquation(String etape)
    {
        String equation = "";

        //Gauche du egal.
        for (int i = 0; i < _leftTerms.size(); i++)
        {
            //S'il y a une parenthese ouvrante avant le terme.
            if (!_leftTerms.get(i).getOpenParenthesis().isEmpty())
            {
                for (String open : _leftTerms.get(i).getOpenParenthesis())
                {
                    equation += (i != 0 ? (" " + open + " ") : "") + "(";
                }
            }
            
            if (i != 0 && equation.charAt(equation.length() - 1) != '(')
            {
                equation += " ";
                
                if (_leftTerms.get(i).getOperator().equals("+") && _leftTerms.get(i).getCoefficient() < 0)
                {
                    equation += "- ";
                }
                else if (_leftTerms.get(i).getOperator().equals("+"))
                {
                    equation += _leftTerms.get(i).getOperator() + " ";
                }
                else if (_leftTerms.get(i).getCoefficient() < 0)
                {
                    equation += _leftTerms.get(i).getOperator() + " -";
                }
                else
                {
                    equation += _leftTerms.get(i).getOperator() + " ";
                }
            }
            else
            {
                if (_leftTerms.get(i).getOperator().equals("+") && _leftTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else if (!_leftTerms.get(i).getOperator().equals("+"))
                {
                    equation += _leftTerms.get(i).getOperator();
                }
            }

            if (Math.abs(_leftTerms.get(i).getExponent()) == 0 || Math.abs(_leftTerms.get(i).getCoefficient()) != 1)
            {
                equation += _df.format(Math.abs(_leftTerms.get(i).getCoefficient()));
            }
            
            if (Math.abs(_leftTerms.get(i).getExponent()) != 0 && Math.abs(_leftTerms.get(i).getCoefficient()) != 0)
            {
                equation += _leftTerms.get(i).getCharacter();
            }

            if (Math.abs(_leftTerms.get(i).getExponent()) != 1 && Math.abs(_leftTerms.get(i).getExponent()) != 0 && Math.abs(_leftTerms.get(i).getCoefficient()) != 0)
            {
                equation += "^";
                equation += _df.format(_leftTerms.get(i).getExponent());
            }
            
            //S'il y a une parenthese fermante apres le terme.
            if (!_leftTerms.get(i).getCloseParenthesis().isEmpty())
            {
                for (String close : _leftTerms.get(i).getCloseParenthesis())
                {
                    equation += ")";
                }
            }
        }

        equation += " = ";

        //Droite du egal.
        for (int i = 0; i < _rightTerms.size(); i++)
        {
            //S'il y a une parenthese ouvrante avant le terme.
            if (!_rightTerms.get(i).getOpenParenthesis().isEmpty())
            {
                for (String open : _rightTerms.get(i).getOpenParenthesis())
                {
                    equation += (i != 0 ? (" " + open + " ") : "") + "(";
                }
            }
            
            if (i != 0 && equation.charAt(equation.length() - 1) != '(')
            {
                equation += " ";
                
                if (_rightTerms.get(i).getOperator().equals("+") && _rightTerms.get(i).getCoefficient() < 0)
                {
                    equation += "- ";
                }
                else if (_rightTerms.get(i).getOperator().equals("+"))
                {
                    equation += _rightTerms.get(i).getOperator() + " ";
                }
                else if (_rightTerms.get(i).getCoefficient() < 0)
                {
                    equation += _rightTerms.get(i).getOperator() + " -";
                }
                else
                {
                    equation += _rightTerms.get(i).getOperator() + " ";
                }
            }
            else
            {
                if (_rightTerms.get(i).getOperator().equals("+") && _rightTerms.get(i).getCoefficient() < 0)
                {
                    equation += "-";
                }
                else if (!_rightTerms.get(i).getOperator().equals("+"))
                {
                    equation += _rightTerms.get(i).getOperator();
                }
            }

            if ((Math.abs(_rightTerms.get(i).getExponent())) == 0 || Math.abs(_rightTerms.get(i).getCoefficient()) != 1)
            {
                equation += _df.format(Math.abs(_rightTerms.get(i).getCoefficient()));
            }
            
            if (Math.abs(_rightTerms.get(i).getExponent()) != 0 && Math.abs(_rightTerms.get(i).getCoefficient()) != 0)
            {
                equation += _rightTerms.get(i).getCharacter();
            }

            if (Math.abs(_rightTerms.get(i).getExponent()) != 1 && Math.abs(_rightTerms.get(i).getExponent()) != 0 && Math.abs(_rightTerms.get(i).getCoefficient()) != 0)
            {
                equation += "^";
                equation += _df.format(_rightTerms.get(i).getExponent());
            }
            
            //S'il y a une parenthese fermante apres le terme.
            if (!_rightTerms.get(i).getCloseParenthesis().isEmpty())
            {
                for (String close : _rightTerms.get(i).getCloseParenthesis())
                {
                    equation += ")";
                }
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
        String equation = "+1*x^2=+(+1*x^1+1*x^0)^2*x^0";

	ArrayList<String> variables = new ArrayList();
	variables.add("x");

	Resolution resolution = new Resolution(equation, variables);
        
        resolution.fillAllTerms();
	resolution.manageParenthesis();
        resolution.solve();
    }
}

//DecimalFormat: http://stackoverflow.com/questions/14204905/java-how-to-remove-trailing-zeros-from-a-double