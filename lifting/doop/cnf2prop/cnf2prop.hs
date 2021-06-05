module Main where 

import System.IO 
import System.Environment
import Data.List.Split
import qualified Data.HashMap.Strict as D

dump :: [String] -> String
dump = foldr (\s t -> (s ++ ", " ++ t)) ""

isID :: [String] -> Bool
isID xs = case xs of
    [h0, h1, index, id] -> (h0 == "c") && (h1 == "c" || h1 == "u")
    _ -> False

isClause :: [String] -> Bool
isClause xs = not(null xs) && (head xs /= "p") && (head xs /= "c")

toPair :: [String] -> (Int, String)
toPair xs = (read (xs !! 2) :: Int, xs !! 3)

--lit2Str :: D.HashMap -> String -> String
lit2str dict lit =
    let index = read lit :: Int 
        absIndex = abs index 
        litText = D.lookup absIndex dict
    in
        case litText of
            Nothing -> error "lit not found"
            Just litText' -> if (index < 0) then ("!" ++ litText') else litText'

cleanupClause :: [String] -> [String]
cleanupClause = filter (\lit -> lit /= "" && lit /= "0")

--clause2Str :: D.HashMap -> [String] -> String
clause2Str dict xs = 
    let xss = (map (lit2str dict) xs) in
        foldl (\a b -> a ++ " \\/ " ++ b) (head xss) (tail xss)

buildConj :: [String] -> String
buildConj xs = foldl (\a b -> a ++ " /\\ " ++ b) (head xs) (tail xs)

main = do
    args <- getArgs
    if (length args) /= 1 then
        putStrLn "Usage: cnf2prop <filename>"
    else do
        c <- readFile (head args)
        let lns = lines c
        let toks = map (splitOn " ") lns
        let ids = D.fromList $ map toPair (filter isID toks)
        let clauses = filter (not . null) $ map cleanupClause (filter isClause toks)
        putStrLn $ buildConj (map (\s -> "(" ++ (clause2Str ids s) ++ ")") clauses)
        