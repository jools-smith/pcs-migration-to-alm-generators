// Test.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <exception>
#include <format>
#include <fstream>
#include <iostream>
#include <stdexcept>
#include <string>
#include <filesystem>


using namespace std;


int main(const int argc, const char* const argv[]) {
  try {
    if (argc < 2) {
      throw runtime_error("No file name provided as argument");
    }

    const string filepath = argv[1];

    if (!filesystem::exists(filepath)) {
      throw runtime_error(std::format("File does not exist : {}", filepath));
    }

    // overwrite file
    ofstream file(filepath, std::ios::out);

    if (!file.is_open()) {
      throw runtime_error(std::format("Failed to open file : {}", filepath));
    }

    for (int i = 0; i < argc; i++) {
      file << argv[i] << endl;
    }

    file.flush();

    file.close();
  }
  catch (const exception& ex) {
    cerr << "Exception: " << ex.what() << endl;
  }
  catch (...) {
    cerr << "Unexpected exception" << endl;
  }
}

