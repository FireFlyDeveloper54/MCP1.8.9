package net.optifine.expr;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TokenParser
{
    public static Token[] parse(String str) throws IOException, ParseException
    {
        Reader reader = new StringReader(str);
        PushbackReader pushbackReader = new PushbackReader(reader);
        List<Token> tokens = new ArrayList();

        while (true)
        {
            int charCode = pushbackReader.read();

            if (charCode < 0)
            {
                Token[] tokenArray = (Token[])((Token[])tokens.toArray(new Token[tokens.size()]));
                return tokenArray;
            }

            char firstCharacter = (char)charCode;

            if (!Character.isWhitespace(firstCharacter))
            {
                TokenType tokenType = TokenType.getTypeByFirstChar(firstCharacter);

                if (tokenType == null)
                {
                    throw new ParseException("Invalid character: \'" + firstCharacter + "\', in: " + str);
                }

                Token token = readToken(firstCharacter, tokenType, pushbackReader);
                tokens.add(token);
            }
        }
    }

    private static Token readToken(char chFirst, TokenType type, PushbackReader pr) throws IOException
    {
        StringBuffer tokenText = new StringBuffer();
        tokenText.append(chFirst);

        while (true)
        {
            int charCode = pr.read();

            if (charCode < 0)
            {
                break;
            }

            char nextCharacter = (char)charCode;

            if (!type.hasCharNext(nextCharacter))
            {
                pr.unread(nextCharacter);
                break;
            }

            tokenText.append(nextCharacter);
        }

        return new Token(type, tokenText.toString());
    }
}
