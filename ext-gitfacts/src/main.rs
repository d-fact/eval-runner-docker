use std::path::{Path};
use git2::{Repository, Revwalk, Oid};
use clap::{Arg, App, ArgMatches, crate_authors, crate_version};
use log::{info};
use env_logger::Env;
use std::fs::{OpenOptions};
use std::io::Write;

fn main() {
    init_log();
    let matches = handle_args();
    let repo_path = Path::new(matches.value_of("INPUT").unwrap());
    let repo: Repository = get_repo(repo_path);
    let rev_range: Vec<&str> = match matches.values_of("RevRange") {
        Some(r) => r.collect::<Vec<&str>>(),
        None => Vec::new()
    };
    let out_file = match matches.value_of("Output") {
        Some(o) => Path::new(o),
        None => Path::new("history.facts")
    };
    let fact_fmt: FactFormat = match matches.value_of("OutFormat") {
        Some("ta") => FactFormat::TA,
        Some("dl") => FactFormat::Souffle,
        _ => {
            info!("Default fact format to Datalog (Souffle)");
            FactFormat::Souffle
        }
    };
    walk_multiple_rev(&repo, &rev_range, out_file, fact_fmt).unwrap();
}

fn init_log() {
    let env = Env::default()
        .filter_or("RUST_LOG", "warn")
        .write_style_or("LOG_STYLE", "always");
    env_logger::init_from_env(env);
}

fn handle_args() -> ArgMatches {
    App::new("Git History Facts Generator")
        .version(crate_version!())
        .author(crate_authors!())
        .arg(Arg::new("INPUT")
            .about("Path to the repo")
            .required(true).index(1))
        .arg(Arg::new("RevRange").short('r')
            .takes_value(true)
            .multiple(true)
            .about("Specify commit ranges"))
        .arg(Arg::new("Output").short('o')
            .takes_value(true)
            .about("Specify output filename, default to history.facts"))
        .arg(Arg::new("OutFormat").short('f')
            .takes_value(true)
            .about("Specify format of output facts, ta/dl, default to dl"))
        .get_matches()
}

fn get_repo(path: &Path) -> Repository {
    match Repository::open(path) {
        Ok(repo) => repo,
        Err(e) => panic!("Failed to open: {}", e),
    }
}

fn find_parents(repo: &Repository, ids: &Vec<Oid>) -> Vec<AncestralFact> {
    let mut vec_of_pairs = vec![];
    info!("Correctly pairing commits for selected revision range [from {} to {}]",
          ids.first().unwrap(), ids.last().unwrap());
    for x in ids {
        let c = repo.find_commit(x.clone()).unwrap();
        for (pindex, p) in c.parents().enumerate() {
            let f = AncestralFact::new(p.id().to_string(), x.to_string(), pindex);
            vec_of_pairs.push(f);
        }
    }
    vec_of_pairs
}

fn walk_multiple_rev(repo: &Repository, revs: &Vec<&str>, out: &Path, ofmt: FactFormat)
                     -> Result<(), git2::Error> {
    if revs.is_empty() {
        let ids = walk_rev(repo, None)?;
        write_facts(find_parents(repo, &ids), out, &ofmt);
    } else {
        for &r in revs {
            let ids = walk_rev(&repo, Some(r))?;
            write_facts(find_parents(repo, &ids), out, &ofmt);
        }
    }
    Ok(())
}

fn walk_rev(repo: &Repository, rev_range: Option<&str>) -> Result<Vec<git2::Oid>, git2::Error> {
    let mut revwalk: Revwalk = repo.revwalk().unwrap();
    revwalk.set_sorting(git2::Sort::TOPOLOGICAL)?;
    match rev_range {
        Some(r) => {
            info!("Processing selected revision range {}", r);
            if r.contains("..") {
                let spec = repo.revparse(r)?;
                revwalk.push(spec.from().unwrap().id())?;
                revwalk.hide(spec.to().unwrap().id())?;
            } else {
                revwalk.push(repo.revparse_single(r)?.id())?;
            }
        }
        None => revwalk.push_head()?
    };
    revwalk.collect::<Result<Vec<git2::Oid>, git2::Error>>()
}


fn write_facts(ids: Vec<AncestralFact>, output: &Path, ofmt: &FactFormat) {
    let mut ofile = OpenOptions::new().create(true).write(true).truncate(true)
        .open(output).unwrap();
    let mut contents = String::new();
    for each in ids {
        match ofmt {
            FactFormat::Souffle => contents += &each.to_dl(),
            FactFormat::TA => contents += &each.to_ta()
        }
    }
    ofile.write_all(contents.as_bytes()).unwrap();
}


pub struct AncestralFact {
    pub parent: String,
    pub child: String,
    pub parent_index: usize,
}

impl AncestralFact {
    pub fn new(p: String, c: String, i: usize) -> AncestralFact {
        AncestralFact { parent: p, child: c, parent_index: i }
    }
    pub fn to_ta(&self) -> String {
        format!("Ancestral {} {}\n", self.child, self.parent).to_string()
    }

    pub fn to_dl(&self) -> String {
        format!("{}\t{}\t{}\n", self.child, self.parent, self.parent_index).to_string()
    }
}

enum FactFormat {
    TA,
    Souffle,
}