from flie.utils.SimpleTree import Formula





def main():

    f = Formula([encodingConstants.LAND,
                 Formula("p"),
                 Formula([encodingConstants.IMPLIES,
                         Formula("p"),
                         Formula("p")
                    ])
                 ])

    normalized = Formula.normalize(f)
    logging.debug(normalized)

if __name__ == '__main__':
    main()