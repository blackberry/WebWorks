/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://www.phonegap.com/about/license/ for full text.
 *
 * Copyright (c) 2011, IBM Corporation
 */

package blackberry.common.util.json4j.internal;

import java.io.IOException;
import java.io.Reader;

import blackberry.common.util.json4j.JSONArray;
import blackberry.common.util.json4j.JSONArtifact;
import blackberry.common.util.json4j.JSONException;
import blackberry.common.util.json4j.JSONObject;

/**
 * Private parser class which handles doing the parsing of the JSON string into tokens.
 */
public class Parser {

    private Tokenizer tokenizer;
    private Token     lastToken;

    private JSONArtifact jArtifact;

    private boolean firstArtifact = false;

    /**
     * Contructor
     * @param reader The Reader to use when reading in the JSON stream/string.
     *
     * @throws JSONException Thrown if an error occurs in tokenizing the JSON string.
     */
    public Parser(Reader reader) throws JSONException {
        super();     
        try {
            this.tokenizer = new Tokenizer(reader, false);
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
    }

    /**
     * Contructor
     * @param reader The Reader to use when reading in the JSON stream/string.
     * @param strict Boolean indicating if the parser should parse in strict mode, meaning unqoted strings and comments are not allowed.
     *
     * @throws JSONException Thrown if an error occurs in tokenizing the JSON string.
     */
    public Parser(Reader reader, boolean strict) throws JSONException {
        super();     
        try {
            this.tokenizer = new Tokenizer(reader, strict);
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * Same as calling parse(false);
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONObject parse() throws JSONException {
        return parse(false, (JSONObject)null);
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * Same as calling parse(false);
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONObject parse(JSONObject jObj) throws JSONException {
        return parse(false, jObj);
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * @param ordered Flag to denote if the parse should contruct a JSON object which maintains serialization order of the attributes.
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONObject parse(boolean ordered) throws JSONException {
        try {
            lastToken = tokenizer.next();
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
        return parseObject(ordered, null);
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * @param ordered Flag to denote if the parse should contruct a JSON object which maintains serialization order of the attributes.
     * @param jObj The JSONObjetc to fill out from the parsing.  If null, create a new one.
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONObject parse(boolean ordered, JSONObject jObj) throws JSONException {
        try {
            lastToken = tokenizer.next();
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
        return parseObject(ordered, jObj);
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * @param jObj The JSONArray to fill out from the parsing.  If null, create a new one.
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONArray parse(JSONArray jObj) throws JSONException {
        return parse(false, jObj);
    }

    /**
     * Method to initiate the parse of the toplevel JSON object, which will in turn parse all child JSON objects contained within.
     * @param ordered Flag to denote if the parse should contruct for all JSON objects encounted, a JSON object which maintains serialization order of the attributes.
     * @param jObj The JSONArray to fill out from the parsing.  If null, create a new one.
     * 
     * @throws JSONException Thrown if an IO error occurd during parse of the JSON object(s).
     */
    public JSONArray parse(boolean ordered, JSONArray jObj) throws JSONException {
        try {
            lastToken = tokenizer.next();
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during input read.");
            jex.setCause(iox);
            throw jex;
        }
        return parseArray(ordered, jObj);
    }

    /**
     * Method to parse a JSON object out of the current JSON string position.
     * @return JSONObject Returns the parsed out JSON object.
     *
     * @throws JSONException Thrown if an IO error occurs during parse, such as a malformed JSON object.
     */
    public JSONObject parseObject() throws JSONException {
        return parseObject(false, null);
    }

    /**
     * Method to parse a JSON object out of the current JSON string position.
     * @param ordered Flag to denote if the parse should contruct a JSON object which maintains serialization order of the attributes.     
     * @return JSONObject Returns the parsed out JSON object.
     *
     * @throws JSONException Thrown if an IO error occurs during parse, such as a malformed JSON object.
     */
    public JSONObject parseObject(boolean ordered, JSONObject rootObject) throws JSONException {

        try {
            JSONObject result = null;
            if (rootObject != null) {
                result = rootObject;
            } else {
                if (!ordered) {
                    result = new JSONObject();
                } else {
                	//MSN NO ORDERED
                    result = new JSONObject();
                }
            }

            if (lastToken != Token.TokenBraceL) throw new JSONException("Expecting '{' " + tokenizer.onLineCol() + " instead, obtained token: '" + lastToken + "'");
            lastToken = tokenizer.next();

            while (true) {
                if (lastToken == Token.TokenEOF) throw new JSONException("Unterminated object " + tokenizer.onLineCol());

                if (lastToken == Token.TokenBraceR) {
                    lastToken = tokenizer.next();
                    break;
                }

                if (!lastToken.isString()) throw new JSONException("Expecting string key " + tokenizer.onLineCol());
                String key = lastToken.getString();

                lastToken = tokenizer.next();
                if (lastToken != Token.TokenColon) throw new JSONException("Expecting colon " + tokenizer.onLineCol());

                lastToken = tokenizer.next();
                Object val = parseValue(ordered);

                result.put(key, val);

                if (lastToken == Token.TokenComma) {
                    lastToken = tokenizer.next();
                }

                else if (lastToken != Token.TokenBraceR) {
                    throw new JSONException("expecting either ',' or '}' " + tokenizer.onLineCol());
                }
            }
            return result;
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during object input read.");
            jex.setCause(iox);
            throw jex;
        }
    }

    /**
     * Method to parse out a JSON array from a JSON string
     * Same as calling parseArray(false, null)
     * 
     * @throws JSONException Thrown if a parse error occurs, such as a malformed JSON array.
     */
    public JSONArray parseArray() throws JSONException {
        return parseArray(false, null);
    }
    
    /**
     * Method to parse out a JSON array from a JSON string
     * @param ordered Flag to denote if the parse should contruct JSON objects which maintain serialization order of the attributes for all JSONOjects in the array.     
     * *param array An array instance to populate instead of creating a new one.
     * 
     * @throws JSONException Thrown if a parse error occurs, such as a malformed JSON array.
     */
    public JSONArray parseArray(boolean ordered, JSONArray array) throws JSONException {
        JSONArray result = null;
        if(array != null){
            result = array;
        } else {
            result = new JSONArray();
        }

        try {
            if (lastToken != Token.TokenBrackL) throw new JSONException("Expecting '[' " + tokenizer.onLineCol());
            lastToken = tokenizer.next();
            while (true) {
                if (lastToken == Token.TokenEOF) throw new JSONException("Unterminated array " + tokenizer.onLineCol());

                /**
                 * End of the array.
                 */
                if (lastToken == Token.TokenBrackR) {
                    lastToken = tokenizer.next();
                    break;
                }

                Object val = parseValue(ordered);
                result.add(val);

                if (lastToken == Token.TokenComma) {
                    lastToken = tokenizer.next();
                } else if (lastToken != Token.TokenBrackR) {
                    throw new JSONException("expecting either ',' or ']' " + tokenizer.onLineCol());
                }
            }
        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during array input read.");
            jex.setCause(iox);
            throw jex;
        }
        return result;
    }

    /**
     * Method to parse the current JSON property value from the last token. 
     * @return The java object type that represents the JSON value.
     *
     * @throws JSONException Thrown if an IO error (read incomplete token) occurs.
     */
    public Object parseValue() throws JSONException {
        return parseValue(false);
    }

    /**
     * Method to parse the current JSON property value from the last token. 
     * @return The java object type that represents the JSON value.
     * @param ordered Flag to denote if the parse should contruct JSON objects and arrays which maintain serialization order of the attributes.     
     *
     * @throws JSONException Thrown if an IO error (read incomplete token) occurs.
     */
    public Object parseValue(boolean ordered) throws JSONException {
        if (lastToken == Token.TokenEOF) throw new JSONException("Expecting property value " + tokenizer.onLineCol());

        try {
            if (lastToken.isNumber()) {
                Object result = lastToken.getNumber();
                lastToken = tokenizer.next();
                return result;
            }

            if (lastToken.isString()) {
                Object result = lastToken.getString();
                lastToken = tokenizer.next();
                return result;
            }

            if (lastToken == Token.TokenFalse) {
                lastToken = tokenizer.next();
                return Boolean.FALSE;
            }

            if (lastToken == Token.TokenTrue) {
                lastToken = tokenizer.next();
                return Boolean.TRUE;
            }

            if (lastToken == Token.TokenNull) {
                lastToken = tokenizer.next();
                return JSONObject.NULL;
            }

            if (lastToken == Token.TokenBrackL) return parseArray(ordered, null);
            if (lastToken == Token.TokenBraceL) return parseObject(ordered, null);

        } catch (IOException iox) {
            JSONException jex = new JSONException("Error occurred during value input read.");
            jex.setCause(iox);
            throw jex;
        }
        throw new JSONException("Invalid token " + tokenizer.onLineCol());
    }
}
