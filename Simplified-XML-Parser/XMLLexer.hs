-- Michael Bartoli
-- cs131 midterm 

module XMLLexer where

import Data.Char

-- type of XML tokens to be used in the parser.
data XMLtoken = Plain String | OpenTag String | CloseTag String
                deriving (Eq,Show)

-- accumUpTo c lst returns a pair whose first element consists of
-- all elements of the list up to, but not including, the first occurrence
-- of  c, while the second element of the pair is the list of those 
-- elements following c.
accumUpTo :: (Eq a) => a -> [a] -> ([a], [a])
accumUpTo c [] = ([],[])
accumUpTo c (fst:rest) = if c == fst then ([],rest)
                                    else let
                                       (before,after) = accumUpTo c rest
                                    in
                                       (fst:before,after)

-- gettokens s returns a list of all XML tokens obtained by analyzing
-- the string s.
gettokens :: [Char] -> [XMLtoken]
gettokens [] = []
gettokens ('<':fst:rest) = if fst /= '/' then
              let 
        (tag,remainder) = accumUpTo '>' (fst:rest);
        upTag = map toUpper tag
              in 
        (OpenTag upTag) : gettokens remainder
      else                        -- opening tag 
              let 
                    (tag,remainder) = accumUpTo '>' rest
                    upTag = map toUpper tag
              in 
        (CloseTag upTag) : gettokens remainder
       
gettokens (fst:rest) =                 -- Untagged text 
          if isSpace fst then
       -- Recurse to skip white space 
       gettokens rest
    else 
              let 
      (tag,remainder) = accumUpTo '<' (fst:rest)
              in 
      if remainder == [] then [Plain tag]
                             else (Plain tag) : gettokens ('<':remainder)



-- my code

data XMLterm = AST_TAG (String,XMLterm) | AST_TEXT String | AST_SEQ (XMLterm,XMLterm) | AST_EMPTY | AST_ERROR (XMLterm,String) deriving (Eq,Show)

-- our main function that takes in a list of XMLtokens
parserString :: [XMLtoken] -> XMLterm
parserString [] = AST_EMPTY
parserString (OpenTag tag:rest) = let
                                   (before, after) = accumUpTo (CloseTag tag) rest
                                 in
                                   if length(before) > 0 then 
                                      if length(after) == 0 then
                                        AST_TAG(tag, parserString before)
                                      else
                                        AST_SEQ(AST_TAG(tag,parserString before), parserString after)
                                   else
                                      AST_TAG (tag,parserString before)
parserString (Plain tag:rest) = AST_TEXT tag 


-- wrapper for our function to fulfill type requirements
parseString :: String -> XMLterm
parseString uv = let tokens = gettokens uv
                 in parserString tokens

prettyPrinter :: XMLterm -> String
prettyPrinter AST_EMPTY = "" 
prettyPrinter (AST_TAG (str,term)) = "<"++str++">"++ prettyPrinter(term) ++"</"++str++">" 
prettyPrinter (AST_TEXT txt) = txt
prettyPrinter (AST_SEQ (term1,term2)) = prettyPrinter term1 ++ prettyPrinter term2

